package rca.ac.rw.template.notification;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.audits.TimestampAudit; 

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends TimestampAudit { 

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Meter number is required for notification")
    @Column(nullable = false, length = 6) 
    private String meterNumber;

    @NotBlank(message = "Notification message is required")
    @Column(nullable = false, columnDefinition = "TEXT") 
    private String message;

    @NotNull(message = "Associated user for notification is required")
    @Column(name="user_email", nullable = false) 
    private String userEmail;


    
    

    public Notification(String meterNumber, String message, String userEmail) {
        this.meterNumber = meterNumber;
        this.message = message;
        this.userEmail = userEmail;
    }
}