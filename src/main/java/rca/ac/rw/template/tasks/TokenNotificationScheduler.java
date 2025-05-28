package rca.ac.rw.template.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rca.ac.rw.template.notification.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenNotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Runs periodically (e.g., every hour) to check for expiring tokens,
     * create notifications, and send email warnings.
     * Also marks truly expired NEW tokens.
     */
    // Cron for every hour at the start of the hour: "0 0 * * * ?"
    // Cron for every 5 minutes: "0 */5 * * * ?"
    // Cron for every minute (for frequent testing): "0 * * * * ?"
    @Scheduled(cron = "${eucl.token.notification.cron:0 0 * * * ?}")
    public void processTokenNotificationsAndCleanup() {
        log.info("SCHEDULER: Starting scheduled task for token expiration notifications and cleanup.");
        try {
            notificationService.processExpiringTokenNotifications();
        } catch (Exception e) {
            log.error("SCHEDULER: Error during scheduled execution of token notification processing: {}", e.getMessage(), e);
        }
        log.info("SCHEDULER: Finished scheduled task for token expiration notifications and cleanup.");
    }
}