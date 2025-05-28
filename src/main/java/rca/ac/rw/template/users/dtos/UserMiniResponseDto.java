package rca.ac.rw.template.users.dtos; 

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMiniResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}