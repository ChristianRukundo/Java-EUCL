package rca.ac.rw.template.users.dtos; // Corrected package based on your import

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.meter.dto.MeterResponseDto; // Import Meter DTO
import rca.ac.rw.template.token.dto.TokenSummaryResponseDto; // Import Token DTO
import rca.ac.rw.template.users.Address;
import rca.ac.rw.template.users.Role;
import rca.ac.rw.template.users.Status;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private Status status;
    private Address address;
    private Role role;
    private boolean enabled;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // EUCL Specific Additions for Customer Profile
    private List<MeterResponseDto> meters;          // List of meters registered to this user
    private List<TokenSummaryResponseDto> newTokens; // List of NEW (unused) tokens for this user's meters
}