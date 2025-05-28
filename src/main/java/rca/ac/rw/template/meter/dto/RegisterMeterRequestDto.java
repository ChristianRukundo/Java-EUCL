package rca.ac.rw.template.meter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMeterRequestDto {

    @NotBlank(message = "Meter number is required")
    @Size(min = 6, max = 6, message = "Meter number must be exactly 6 digits long")
    @Pattern(regexp = "^\\d{6}$", message = "Meter number must be exactly 6 digits (0-9)") // Only digits
    private String meterNumber;

    @NotNull(message = "User ID for meter registration is required")
    private UUID userId;
}