package rca.ac.rw.template.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.token.TokenStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenSummaryResponseDto {
    private String tokenString; // Original unformatted
    private String formattedTokenString;
    private Integer daysOfElectricity;
    private BigDecimal amountPaid;
    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;
    private TokenStatus status;
}