package rca.ac.rw.template.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.commons.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.commons.exceptions.ValidationException;
import rca.ac.rw.template.meter.Meter;
import rca.ac.rw.template.meter.MeterRepository;
import rca.ac.rw.template.token.dto.*;
import rca.ac.rw.template.users.User;
import rca.ac.rw.template.users.UserService;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final PurchasedTokenRepository purchasedTokenRepository;
    private final MeterRepository meterRepository;
    private final UserService userService; // To get authenticated user
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MIN_PURCHASE_AMOUNT = 100;
    private static final int RWF_PER_DAY = 100;
    private static final int MAX_DAYS_PURCHASE = 5 * 365; // 5 years

    /**
     * Allows a customer to purchase electricity for a specific meter.
     * Generates a token based on the amount paid.
     *
     * @param user        The authenticated customer making the purchase.
     * @param purchaseDto DTO containing meter number and amount.
     * @return GeneratedTokenResponseDto with token details.
     */
    @Transactional
    public GeneratedTokenResponseDto purchaseElectricity(User user, PurchaseElectricityRequestDto purchaseDto) {
        log.info("User {} attempting to purchase electricity for meter {} with amount {}",
                user.getEmail(), purchaseDto.getMeterNumber(), purchaseDto.getAmount());


        Meter meter = meterRepository.findByMeterNumber(purchaseDto.getMeterNumber())
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "number", purchaseDto.getMeterNumber()));


        if (!meter.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to purchase electricity for meter {} which they do not own.",
                    user.getEmail(), meter.getMeterNumber());
            throw new ValidationException("You do not own this meter number: " + meter.getMeterNumber());
        }

        BigDecimal amount = purchaseDto.getAmount();
        if (amount.compareTo(BigDecimal.valueOf(MIN_PURCHASE_AMOUNT)) < 0) {
            throw new ValidationException("Minimum purchase amount is " + MIN_PURCHASE_AMOUNT + " RWF.");
        }
        if (amount.remainder(BigDecimal.valueOf(RWF_PER_DAY)).compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Purchase amount must be a multiple of " + RWF_PER_DAY + " RWF.");
        }

        // 4. Calculate Days of Electricity
        // No half-day tokens, so integer division.
        int daysOfElectricity = amount.divide(BigDecimal.valueOf(RWF_PER_DAY), 0, RoundingMode.DOWN).intValue();
        if (daysOfElectricity > MAX_DAYS_PURCHASE) {
            throw new ValidationException("Purchase cannot exceed " + MAX_DAYS_PURCHASE + " days (" + MAX_DAYS_PURCHASE / 365 + " years) of electricity.");
        }
        if (daysOfElectricity <= 0) { // Should be caught by min amount, but good check
            throw new ValidationException("Amount too low to generate a valid token duration.");
        }


        // 5. Generate 16-digit Token String
        String tokenString = generate16DigitToken();
        // Ensure uniqueness (highly unlikely collision, but good practice for critical systems)
        while (purchasedTokenRepository.findByTokenString(tokenString).isPresent()) {
            log.warn("Generated token {} already exists, regenerating.", tokenString);
            tokenString = generate16DigitToken();
        }

        // 6. Create and Save PurchasedToken Entity
        LocalDateTime purchasedAt = LocalDateTime.now();
        LocalDateTime expiresAt = purchasedAt.plusDays(daysOfElectricity);

        PurchasedToken purchasedToken = new PurchasedToken();
        purchasedToken.setMeter(meter);
        purchasedToken.setTokenString(tokenString);
        purchasedToken.setTokenStatus(TokenStatus.NEW);
        purchasedToken.setTokenValueDays(daysOfElectricity);
        purchasedToken.setAmount(amount);
        // purchasedToken.setCreatedAt(purchasedAt); // Handled by @TimestampAudit
        purchasedToken.setExpiresAt(expiresAt); // Set the calculated expiry

        PurchasedToken savedToken = purchasedTokenRepository.save(purchasedToken);
        log.info("User {} successfully purchased electricity. Token: {}, Days: {}, Meter: {}",
                user.getEmail(), savedToken.getTokenString(), daysOfElectricity, meter.getMeterNumber());

        // We need to manually set 'purchased_date' in the entity if it is separate from createdAt from TimestampAudit
        // However, our PurchasedToken uses createdAt from TimestampAudit as the purchased_date.
        // So, savedToken.getCreatedAt() will give the purchase time.

        return PurchasedTokenConverter.toGeneratedTokenResponseDto(savedToken);
    }

    private String generate16DigitToken() {
        StringBuilder token = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            token.append(SECURE_RANDOM.nextInt(10)); // 0-9
        }
        return token.toString();
    }

    /**
     * Validates a given token string.
     * If valid and NEW, it's marked as USED.
     * Displays meter number, days of electricity, status, and formatted token.
     *
     * @param validateDto DTO containing the token string.
     * @return ValidatedTokenResponseDto with token details.
     */
    @Transactional
    public ValidatedTokenResponseDto validateToken(ValidateTokenRequestDto validateDto) {
        String normalizedTokenString = PurchasedTokenConverter.normalizeTokenString(validateDto.getTokenString());
        log.info("Attempting to validate token: {} (normalized: {})", validateDto.getTokenString(), normalizedTokenString);

        if (normalizedTokenString == null || normalizedTokenString.length() != 16) {
            throw new ValidationException("Invalid token format. Token must be 16 digits.");
        }

        PurchasedToken token = purchasedTokenRepository.findByTokenString(normalizedTokenString)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "value", normalizedTokenString + " (or it's invalid/expired)"));

        // Check if token is expired based on stored expiresAt
        if (token.getTokenStatus() != TokenStatus.EXPIRED && token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Token {} found, but it has expired at {}. Marking as EXPIRED.", token.getTokenString(), token.getExpiresAt());
            token.setTokenStatus(TokenStatus.EXPIRED);

        }



        if (token.getTokenStatus() == TokenStatus.NEW) {
            log.info("Token {} is NEW. Marking as USED.", token.getTokenString());
            token.setTokenStatus(TokenStatus.USED);
            purchasedTokenRepository.save(token);
        } else if (token.getTokenStatus() == TokenStatus.USED) {
            log.info("Token {} has already been USED.", token.getTokenString());
             throw new ValidationException("Token " + token.getTokenString() + " has been used.");
        } else if (token.getTokenStatus() == TokenStatus.EXPIRED) {
            log.info("Token {} is EXPIRED.", token.getTokenString());
             throw new ValidationException("Token " + token.getTokenString() + " has expired.");
        }

        return PurchasedTokenConverter.toValidatedTokenResponseDto(token);
    }

    /**
     * Retrieves all tokens generated for a specific meter number, owned by the current user.
     *
     * @param meterNumber The meter number string.
     * @param pageable Pagination information.
     * @return Page of TokenSummaryResponseDto.
     */
    @Transactional(readOnly = true)
    public Page<TokenSummaryResponseDto> getTokensByMeterNumberForCurrentUser(String meterNumber, Pageable pageable) {
        User currentUser = userService.getAuthenticatedUser();
        log.info("User {} fetching tokens for their meter number: {}", currentUser.getEmail(), meterNumber);

        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "number", meterNumber));

        // Verify the current user owns this meter
        if (!meter.getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to fetch tokens for meter {} which they do not own.",
                    currentUser.getEmail(), meter.getMeterNumber());
            throw new ValidationException("You are not authorized to view tokens for this meter: " + meter.getMeterNumber());
        }

        // Use Specification for consistency and potential future filtering
        Specification<PurchasedToken> spec = PurchasedTokenSpecifications.findByMeter(meter);
        Page<PurchasedToken> tokenPage = purchasedTokenRepository.findAll(spec, pageable);
        // Or directly: Page<PurchasedToken> tokenPage = purchasedTokenRepository.findByMeter(meter, pageable);
        // if you add that method to PurchasedTokenRepository.

        List<TokenSummaryResponseDto> dtos = tokenPage.getContent().stream()
                .map(PurchasedTokenConverter::toTokenSummaryResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, tokenPage.getTotalElements());
    }

    /**
     * Retrieves all tokens generated for a specific meter number (Admin access).
     *
     * @param meterNumber The meter number string.
     * @param pageable Pagination information.
     * @return Page of TokenSummaryResponseDto.
     */
    @Transactional(readOnly = true)
    public Page<TokenSummaryResponseDto> getAllTokensByMeterNumberAdmin(String meterNumber, Pageable pageable) {
        log.info("Admin fetching all tokens for meter number: {}", meterNumber);

        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "number", meterNumber));

        Specification<PurchasedToken> spec = PurchasedTokenSpecifications.findByMeter(meter);
        Page<PurchasedToken> tokenPage = purchasedTokenRepository.findAll(spec, pageable);

        List<TokenSummaryResponseDto> dtos = tokenPage.getContent().stream()
                .map(PurchasedTokenConverter::toTokenSummaryResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, tokenPage.getTotalElements());
    }
}