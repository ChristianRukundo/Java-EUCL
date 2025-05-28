package rca.ac.rw.template.meter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.users.dtos.UserMiniResponseDto; // A simplified DTO for user info

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeterResponseDto {
    private UUID id;
    private String meterNumber;
    private UserMiniResponseDto user; // Show who owns the meter
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}