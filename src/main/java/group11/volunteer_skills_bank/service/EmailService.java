package group11.volunteer_skills_bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.model.Volunteer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService 
{
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private OrganizationService organizationService;

    public ResponseEntity<?> sendForgotPasswordEmail(String email) throws MessagingException 
    {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        String userId = null;
        String userType = null;
        Volunteer volunteer = volunteerService.findVolunteerByEmail(email);
        if (volunteer != null) {
            userId = volunteer.getVolunteerId();
            userType = "volunteer";
        }
        else {
            Organization organization = organizationService.findOrganizationByEmail(email).block();
            if (organization != null) {
                userId = organization.getOrganizationId();
                userType = "organization";
            }

        }
        

        if (userId!=null && userType!=null) {
            // 2. Create a token AND save it to the database with an expiration date.
            // This service handles the logic of creating and saving the token.
            String token = passwordResetTokenService.createToken(userId,userType);

            // 3. Create the full reset link pointing to your FRONTEND application
            String resetLink = "http://localhost:8081/reset-password?token=" + token;

            // --- Create and send the email as before ---
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("volunteer_skills_bank <kira84174@gmail.com>");
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            String htmlBody = "<h2>Password Reset Request</h2>"
                    + "<p>You have requested to reset your password. Click the button below to set a new one. This link will expire in 15 minutes.</p>"
                    + "<a href=\"" + resetLink
                    + "\" style=\"background-color:#007BFF; color:white; padding:14px 25px; text-align:center; text-decoration:none; display:inline-block; border-radius:8px;\">Reset Password</a>"
                    + "<p><br>If you did not make this request, please ignore this email.</p>";
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
        }
        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
    }
}
