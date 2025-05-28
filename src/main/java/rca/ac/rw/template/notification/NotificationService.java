package rca.ac.rw.template.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.email.EmailService;
import rca.ac.rw.template.meter.Meter;
import rca.ac.rw.template.token.PurchasedToken;
import rca.ac.rw.template.token.PurchasedTokenRepository;
import rca.ac.rw.template.token.TokenStatus;
import rca.ac.rw.template.users.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PurchasedTokenRepository purchasedTokenRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;


    // @Value("${eucl.token.expiration.warning.hours:5}")
    private static final int EXPIRATION_WARNING_HOURS = 5;

    /**
     * Checks for tokens expiring soon, creates notifications, and sends email alerts.
     * This method can be called by a scheduled task or a manual trigger.
     */
    @Transactional
    public void processExpiringTokenNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningWindowEnd = now.plusHours(EXPIRATION_WARNING_HOURS);

        log.info("Processing token expiration notifications. Checking for tokens expiring between {} and {}", now, warningWindowEnd);

        List<PurchasedToken> expiringSoonTokens = purchasedTokenRepository.findByTokenStatusAndExpiresAtBetween(
                TokenStatus.NEW,
                now,
                warningWindowEnd
        );

        if (expiringSoonTokens.isEmpty()) {
            log.info("No NEW tokens found expiring in the next {} hours.", EXPIRATION_WARNING_HOURS);
            markTrulyExpiredTokens(now);
            return;
        }

        log.info("Found {} NEW token(s) expiring soon. Processing notifications...", expiringSoonTokens.size());

        for (PurchasedToken token : expiringSoonTokens) {
            if (token.getMeter() == null || token.getMeter().getUser() == null) {
                log.warn("Token ID {} (Value: {}) is missing meter or user information. Skipping notification.", token.getId(), token.getTokenString());
                continue;
            }
            Meter meter = token.getMeter();
            User user = meter.getUser();

            long hoursUntilExpiry = ChronoUnit.HOURS.between(now, token.getExpiresAt());
            if (hoursUntilExpiry < 0) hoursUntilExpiry = 0;

            String message = String.format(
                    "Dear %s %s, your electricity token for meter %s (Token: %s...) is going to expire in approximately %d hour(s). Please purchase a new token to avoid interruption.",
                    user.getFirstName(),
                    user.getLastName(),
                    meter.getMeterNumber(),
                    token.getTokenString().substring(0, Math.min(token.getTokenString().length(), 4)),
                    hoursUntilExpiry
            );

            Notification notification = new Notification(meter.getMeterNumber(), message, user.getEmail());
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Saved notification ID {} for token ID {} (meter {}), target user email {}",
                    savedNotification.getId(), token.getId(), meter.getMeterNumber(), user.getEmail());

            emailService.sendTokenExpirationWarningEmail(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    meter.getMeterNumber(),
                    token.getTokenString(),
                    hoursUntilExpiry
            );
        }
        markTrulyExpiredTokens(now);
    }

    /**
     * Marks NEW tokens whose expiration time has already passed as EXPIRED.
     * This can be called as part of the notification process or separately.
     * @param currentTime The current time to check against.
     */
    @Transactional
    public void markTrulyExpiredTokens(LocalDateTime currentTime) {
        List<PurchasedToken> pastExpiryTokens = purchasedTokenRepository.findByTokenStatusAndExpiresAtBefore(
                TokenStatus.NEW,
                currentTime
        );
        if (!pastExpiryTokens.isEmpty()) {
            log.info("Found {} NEW token(s) that have passed their expiration time (before {}). Marking as EXPIRED.",
                    pastExpiryTokens.size(), currentTime);
            for (PurchasedToken token : pastExpiryTokens) {
                token.setTokenStatus(TokenStatus.EXPIRED);
                purchasedTokenRepository.save(token);
                log.debug("Marked token ID {} (Meter: {}, Value: {}) as EXPIRED.",
                        token.getId(), token.getMeter().getMeterNumber(), token.getTokenString());
            }
        } else {
            log.info("No NEW tokens found that have passed their expiration time (before {}).", currentTime);
        }
    }
}