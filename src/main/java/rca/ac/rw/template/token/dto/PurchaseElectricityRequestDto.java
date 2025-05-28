package rca.ac.rw.template.token.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseElectricityRequestDto {

    @NotBlank(message = "Meter number is required")
    @Size(min = 6, max = 6, message = "Meter number must be exactly 6 digits long")
    @Pattern(regexp = "^\\d{6}$", message = "Meter number must be exactly 6 digits (0-9)") // Only digits
    private String meterNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.0", message = "Minimum purchase amount is 100 RWF")
    private BigDecimal amount;
}