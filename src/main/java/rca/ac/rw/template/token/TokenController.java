package rca.ac.rw.template.token;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; // Import PageableDefault
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.token.dto.*; // Import all DTOs from this package
import rca.ac.rw.template.users.UserService;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;
    private final UserService userService; // For getAuthenticatedUser

    /**
     * POST /api/v1/tokens/purchase : Customer purchases electricity.
     */
    @PostMapping("/purchase")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<GeneratedTokenResponseDto> purchaseElectricity(
            @Valid @RequestBody PurchaseElectricityRequestDto purchaseDto) {
        var authenticatedCustomer = userService.getAuthenticatedUser();
        GeneratedTokenResponseDto generatedToken = tokenService.purchaseElectricity(authenticatedCustomer, purchaseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(generatedToken);
    }

    /**
     * POST /api/v1/tokens/validate : Customer validates a token.
     * (Task 4a)
     */
    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ValidatedTokenResponseDto> validateToken(
            @Valid @RequestBody ValidateTokenRequestDto validateDto) {
        log.info("API request to validate token: {}", validateDto.getTokenString());
        ValidatedTokenResponseDto validationResult = tokenService.validateToken(validateDto);
        return ResponseEntity.ok(validationResult);
    }

    /**
     * GET /api/v1/tokens/by-meter/{meterNumber} : Customer views their tokens for a specific meter.
     * (Task 4b for customer)
     */
    @GetMapping("/by-meter/{meterNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<TokenSummaryResponseDto>> getMyTokensByMeterNumber(
            @PathVariable String meterNumber,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",         // Property name
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        log.info("API request by customer to get tokens for meter: {}", meterNumber);
        Page<TokenSummaryResponseDto> tokens = tokenService.getTokensByMeterNumberForCurrentUser(meterNumber, pageable);
        return ResponseEntity.ok(tokens);
    }
}