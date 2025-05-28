package rca.ac.rw.template.token;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.stereotype.Repository;
import rca.ac.rw.template.meter.Meter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasedTokenRepository extends JpaRepository<PurchasedToken, Long>, JpaSpecificationExecutor<PurchasedToken> {
    Optional<PurchasedToken> findByTokenString(String tokenString);
//    List<PurchasedToken> findByMeter(Meter meter);
     Page<PurchasedToken> findByMeter(Meter meter, Pageable pageable);
    List<PurchasedToken> findByMeterAndTokenStatus(Meter meter, TokenStatus status);

    /**
     * Finds tokens that are currently NEW and are set to expire within a given time window.
     *
     * @param status The status to check (typically NEW).
     * @param windowStartTime The start of the expiration window (e.g., now).
     * @param windowEndTime The end of the expiration window (e.g., 5 hours from now).
     * @return A list of purchased tokens nearing expiration.
     */
    List<PurchasedToken> findByTokenStatusAndExpiresAtBetween(TokenStatus status, LocalDateTime windowStartTime, LocalDateTime windowEndTime);

    /**
     * Finds tokens that are NEW and have already passed their expiration time
     * but haven't been marked as EXPIRED yet.
     * @param status The status to check (typically NEW).
     * @param currentTime The current time.
     * @return A list of tokens that should be marked as EXPIRED.
     */
    List<PurchasedToken> findByTokenStatusAndExpiresAtBefore(TokenStatus status, LocalDateTime currentTime);
}