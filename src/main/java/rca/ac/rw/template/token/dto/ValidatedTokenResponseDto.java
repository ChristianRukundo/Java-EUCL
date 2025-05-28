package rca.ac.rw.template.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.token.TokenStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedTokenResponseDto {
    private String meterNumber;
    private String tokenString;
    private String formattedTokenString;
    private Integer daysOfElectricity;
    private TokenStatus status;
    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;
}