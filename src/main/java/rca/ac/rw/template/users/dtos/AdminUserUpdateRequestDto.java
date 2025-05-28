package rca.ac.rw.template.users.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.commons.validation.ValidRwandanPhoneNumber;
import rca.ac.rw.template.users.Role;
import rca.ac.rw.template.users.Status;

/**
 * DTO for an admin to update user details.
 * Allows modification of fields like role, status, enabled status.
 * Password changes for other users by admin should be a separate, secure flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequestDto {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName; 

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName; 


    @ValidRwandanPhoneNumber
    private String phoneNumber; 


    private Role role; 

    private Status status; 

    private Boolean enabled; 

    @Valid
    private AddressDto address; 


}