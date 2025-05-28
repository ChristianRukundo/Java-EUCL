package rca.ac.rw.template.token;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.audits.TimestampAudit; // For purchased_date and updated_at
import rca.ac.rw.template.meter.Meter;

import java.math.BigDecimal; // For amount
import java.time.LocalDateTime;


@Entity
@Table(name = "purchased_tokens", indexes = {
        @Index(name = "idx_purchasedtokens_token_unique", columnList = "tokenString", unique = true),
        @Index(name = "idx_purchasedtokens_meter", columnList = "meter_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedToken extends TimestampAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Meter is required for a purchased token")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @NotBlank(message = "Token string is required")
    @Size(min = 16, max = 16, message = "Token must be 16 characters long")
    @Column(nullable = false, unique = true, length = 16, name = "token_string")
    private String tokenString;

    @NotNull(message = "Token status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus tokenStatus;

    @NotNull(message = "Token value in days is required")
    @Min(value = 0, message = "Token value days cannot be negative") // 0 days if amount < 100
    @Column(nullable = false)
    private Integer tokenValueDays;


    @NotNull(message = "Amount paid is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public PurchasedToken(Meter meter, String tokenString, TokenStatus tokenStatus, Integer tokenValueDays, BigDecimal amount, LocalDateTime purchasedDate) {
        this.meter = meter;
        this.tokenString = tokenString;
        this.tokenStatus = tokenStatus;
        this.tokenValueDays = tokenValueDays;
        this.amount = amount;
        if (purchasedDate != null && tokenValueDays != null) {
            this.expiresAt = purchasedDate.plusDays(this.tokenValueDays);
        }

    }
}