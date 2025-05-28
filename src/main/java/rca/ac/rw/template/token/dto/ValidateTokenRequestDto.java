package rca.ac.rw.template.token.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenRequestDto {

    @NotBlank(message = "Token string is required for validation")
    @Size(min = 16, max = 19, message = "Token string must be 16 digits or 19 characters with hyphens")
    @Pattern(regexp = "^(\\d{16}|\\d{4}-\\d{4}-\\d{4}-\\d{4})$", message = "Invalid token format. Expected 16 digits or XXXX-XXXX-XXXX-XXXX")
    private String tokenString;
}