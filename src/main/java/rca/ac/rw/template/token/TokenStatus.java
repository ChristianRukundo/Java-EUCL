package rca.ac.rw.template.token;

public enum TokenStatus {
    NEW,    // Token generated, not yet used/validated by customer's meter
    USED,   // Token has been successfully entered into a meter
    EXPIRED // Token's validity period (days) has passed, or it was explicitly expired
}