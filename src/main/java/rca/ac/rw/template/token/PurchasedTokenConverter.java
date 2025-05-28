package rca.ac.rw.template.token;

import rca.ac.rw.template.token.dto.GeneratedTokenResponseDto;
import rca.ac.rw.template.token.dto.TokenSummaryResponseDto; // Import
import rca.ac.rw.template.token.dto.ValidatedTokenResponseDto;

public class PurchasedTokenConverter {

    public static GeneratedTokenResponseDto toGeneratedTokenResponseDto(PurchasedToken tokenEntity) {
        if (tokenEntity == null) return null;
        return new GeneratedTokenResponseDto(
                tokenEntity.getMeter().getMeterNumber(),
                tokenEntity.getTokenString(),
                formatTokenString(tokenEntity.getTokenString()),
                tokenEntity.getTokenValueDays(),
                tokenEntity.getAmount(),
                tokenEntity.getCreatedAt(),
                tokenEntity.getExpiresAt(),
                tokenEntity.getTokenStatus()
        );
    }

    public static ValidatedTokenResponseDto toValidatedTokenResponseDto(PurchasedToken tokenEntity) {
        if (tokenEntity == null) return null;
        return new ValidatedTokenResponseDto(
                tokenEntity.getMeter().getMeterNumber(),
                tokenEntity.getTokenString(),
                formatTokenString(tokenEntity.getTokenString()),
                tokenEntity.getTokenValueDays(),
                tokenEntity.getTokenStatus(),
                tokenEntity.getCreatedAt(), // purchasedAt
                tokenEntity.getExpiresAt()
        );
    }

    public static TokenSummaryResponseDto toTokenSummaryResponseDto(PurchasedToken tokenEntity) {
        if (tokenEntity == null) return null;
        return new TokenSummaryResponseDto(
                tokenEntity.getTokenString(),
                formatTokenString(tokenEntity.getTokenString()),
                tokenEntity.getTokenValueDays(),
                tokenEntity.getAmount(),
                tokenEntity.getCreatedAt(), // purchasedAt
                tokenEntity.getExpiresAt(),
                tokenEntity.getTokenStatus()
        );
    }

    public static String formatTokenString(String token) {
        if (token == null || token.length() != 16) {
            return token;
        }
        return String.format("%s-%s-%s-%s",
                token.substring(0, 4),
                token.substring(4, 8),
                token.substring(8, 12),
                token.substring(12, 16));
    }

    // Helper to normalize token input (remove hyphens)
    public static String normalizeTokenString(String tokenWithOrWithoutHyphens) {
        if (tokenWithOrWithoutHyphens == null) {
            return null;
        }
        return tokenWithOrWithoutHyphens.replace("-", "");
    }
}