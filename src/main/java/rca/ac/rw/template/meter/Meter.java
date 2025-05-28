package rca.ac.rw.template.meter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.audits.InitiatorAudit; 
import rca.ac.rw.template.token.PurchasedToken; 
import rca.ac.rw.template.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meters", indexes = {
        @Index(name = "idx_meter_number_unique", columnList = "meterNumber", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true) 
@NoArgsConstructor
@AllArgsConstructor
public class Meter extends InitiatorAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) 
    private UUID id;

    @NotBlank(message = "Meter number is required")
    @Size(min = 6, max = 6, message = "Meter number must be exactly 6 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]{6}$", message = "Meter number must be 6 alphanumeric characters") 
    @Column(nullable = false, unique = true, length = 6)
    private String meterNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) 
    private rca.ac.rw.template.users.User user; 

    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchasedToken> purchasedTokens = new ArrayList<>();

    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    public Meter(String meterNumber, User user) {
        this.meterNumber = meterNumber;
        this.user = user;
    }
}