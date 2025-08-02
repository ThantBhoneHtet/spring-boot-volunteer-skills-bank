package group11.volunteer_skills_bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.service.EmailService;
import group11.volunteer_skills_bank.service.PasswordResetTokenService;
import jakarta.mail.MessagingException;

import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth")
public class EmailController 
{
    private EmailService emailService;

    private PasswordResetTokenService passwordResetTokenService;
    
    public EmailController(EmailService emailService, PasswordResetTokenService passwordResetTokenService)
    {
        this.emailService = emailService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> emailRequest) throws MessagingException {
        return emailService.sendForgotPasswordEmail(emailRequest.get("email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            // Call the service to do the work
            passwordResetTokenService.resetPassword(request.get("token"), request.get("newPassword"));

            // If the service completes without an error, return 200 OK
            return ResponseEntity.ok("Password has been reset successfully.");

        } catch (RuntimeException e) {
            // If the service throws ANY exception, catch it and return a 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
