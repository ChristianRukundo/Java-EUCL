package rca.ac.rw.template.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rca.ac.rw.template.notification.NotificationService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/utils")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminUtilityController {

    private final NotificationService notificationService;

    /**
     * Manually triggers the process to check for expiring tokens, create notifications,
     * and send email warnings.
     *
     * @return ResponseEntity indicating the outcome.
     */
    @PostMapping("/trigger-token-expiration-check")
    public ResponseEntity<String> triggerTokenExpirationProcessing() {
        log.info("ADMIN TRIGGER: Initiating token expiration notification processing.");
        try {
            notificationService.processExpiringTokenNotifications();
            String message = "Token expiration notification process triggered successfully and completed.";
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            String errorMessage = "Error during manually triggered token expiration processing: " + e.getMessage();
            log.error(errorMessage, e);
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }

    /**
     * Manually triggers the process to mark any NEW tokens that have already passed
     * their expiration time as EXPIRED.
     *
     * @return ResponseEntity indicating the outcome.
     */
    @PostMapping("/trigger-mark-overdue-tokens-expired")
    public ResponseEntity<String> triggerMarkOverdueTokensAsExpired() {
        log.info("ADMIN TRIGGER: Initiating marking of overdue NEW tokens as EXPIRED.");
        try {
            notificationService.markTrulyExpiredTokens(LocalDateTime.now());
            String message = "Process to mark overdue NEW tokens as EXPIRED triggered successfully and completed.";
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            String errorMessage = "Error during manually triggered marking of overdue tokens: " + e.getMessage();
            log.error(errorMessage, e);
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}