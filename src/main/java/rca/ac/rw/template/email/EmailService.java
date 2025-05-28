package rca.ac.rw.template.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import rca.ac.rw.template.auth.OtpType; 

import java.math.BigDecimal; 

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine; 

    
    @Async
    public void sendAccountVerificationEmail(String to, String name, String otp) {
        sendOtpEmail(to, name, otp, OtpType.VERIFY_ACCOUNT);
    }

    @Async
    public void sendResetPasswordOtp(String to, String name, String otp) {
        sendOtpEmail(to, name, otp, OtpType.FORGOT_PASSWORD);
    }

    @Async
    public void sendVerificationSuccessEmail(String to, String name) {
        sendSuccessEmail(to, name, "verify_success", "Account Verified Successfully");
    }

    @Async
    public void sendResetPasswordSuccessEmail(String to, String name) {
        sendSuccessEmail(to, name, "reset_success", "Password Reset Successfully");
    }





    /**
     * Sends an email notification to the customer about their token nearing expiration.
     *
     * @param to The email address of the customer.
     * @param customerFullName The full name of the customer.
     * @param meterNumber The customer's meter number.
     * @param tokenString The token string that is expiring (can be partial or formatted).
     * @param hoursUntilExpiry Approximate hours until the token expires.
     */
    @Async
    public void sendTokenExpirationWarningEmail(String to, String customerFullName,
                                                String meterNumber, String tokenString,
                                                long hoursUntilExpiry) {
        final String TEMPLATE_NAME = "token_expiration_warning"; 
        final String SUBJECT = "Electricity Token Expiration Warning for Meter: " + meterNumber;
        log.debug("Preparing to send '{}' email to '{}' for meter '{}'", SUBJECT, to, meterNumber);
        try {
            Context context = new Context();
            context.setVariable("name", customerFullName);
            context.setVariable("meterNumber", meterNumber);
            context.setVariable("tokenIdentifier", tokenString.length() > 4 ? tokenString.substring(0,4) + "..." : tokenString); 
            context.setVariable("hoursUntilExpiry", hoursUntilExpiry);
            context.setVariable("companyName", "EUCL"); 

            sendEmailWithTemplate(TEMPLATE_NAME, context, to, SUBJECT);
        } catch (Exception e) {
            log.error("Error preparing or sending token expiration warning email to {}: {}", to, e.getMessage(), e);
        }
    }
    private void sendEmailWithTemplate(String templateName, Context context, String to, String subject) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to process template '{}' for email [{}] to {}: {}", templateName, subject, to, e.getMessage(), e);
        }
    }





    

    private void sendOtpEmail(String to, String name, String otp, OtpType otpType) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);
            context.setVariable("companyName", "EUCL Electric Billing");
            context.setVariable("expirationTimeMinutes", "10"); 

            String subject;
            String templateName;

            switch (otpType) {
                case VERIFY_ACCOUNT:
                    templateName = "verify_account"; 
                    subject = "Verify your account - One Time Password (OTP)";
                    break;
                case FORGOT_PASSWORD:
                    templateName = "forgot_password"; 
                    subject = "Reset your password - One Time Password (OTP)";
                    break;
                default:
                    log.error("Invalid OtpType detected for email sending: {}", otpType);
                    return; 
            }
            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) { 
            log.error("Unable to prepare or send OTP email to {} for type {}: {}", to, otpType, e.getMessage(), e);
        }
    }

    private void sendSuccessEmail(String to, String name, String templateName, String subject) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("companyName", "Rwanda Revenue Authority");
            

            String htmlContent = templateEngine.process(templateName, context); 
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Unable to prepare or send success email '{}' to {}: {}", subject, to, e.getMessage(), e);
        }
    }



    /**
     * Core method to send an HTML email.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("noreply@rra.com"); 
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); 
            mailSender.send(mimeMessage);
            log.info("Successfully sent email '{}' to {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email '{}' to {}: {}", subject, to, e.getMessage(), e);
        }
    }
}