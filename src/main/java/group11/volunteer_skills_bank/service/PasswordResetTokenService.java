package group11.volunteer_skills_bank.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.model.PasswordResetToken;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.repository.OrganizationRepo;
import group11.volunteer_skills_bank.repository.PasswordResetTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordResetTokenService 
{
    private static final int EXPIRATION_MINUTES = 15;

    @Autowired
    private  PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private OrganizationRepo organizationRepo;
    
    public String createToken(String userId, String userType) {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(rawToken);

        // 3. Calculate the expiration date (15 minutes from now)
        Instant now = Instant.now();
        Instant expiration = now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);
        Timestamp expiryDate = Timestamp.of(java.sql.Timestamp.from(expiration));

        // 4. Create the token object with the generic userId and userType
        PasswordResetToken resetToken = new PasswordResetToken(
                hashedToken,
                userId, // Use the provided userId
                userType, // Use the provided userType
                expiryDate);

        // 5. Save the token to the database
        tokenRepository.save(resetToken).block();

        // 6. Return the ORIGINAL, UNHASHED token for the email link
        return rawToken;
    }
    
    public void resetPassword(String rawToken, String newPassword) {
        // 1. Find the token document in the database
        PasswordResetToken token = findAndValidateToken(rawToken); // Assumes this helper method exists

        // 2. Use the userType from the token to determine which user to update
        switch (token.getUserType()) {
            case "volunteer":
                Volunteer volunteer = volunteerService.findVolunteerById(token.getUserId()).block();
                if (volunteer == null) throw new RuntimeException("Volunteer not found.");
                volunteer.setPassword(passwordEncoder.encode(newPassword));
                volunteerService.saveVolunteer(volunteer);
                break;

            case "organization":
                Organization organization = organizationRepo.findById(token.getUserId()).block();
                if (organization == null) throw new RuntimeException("Organization not found.");
                organization.setPassword(passwordEncoder.encode(newPassword));
                organizationRepo.save(organization).block();
                break;

            default:
                throw new RuntimeException("Invalid user type in token.");
        }

        // 3. Delete the token so it cannot be used again
        tokenRepository.deleteById(token.getHashedToken()).block();
    }

    private PasswordResetToken findAndValidateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new RuntimeException("Reset token cannot be empty.");
        }
        List<PasswordResetToken> allTokens = tokenRepository.findAll().collectList().block();

        if (allTokens == null) {
            throw new RuntimeException("Invalid password reset token.");
        }

        PasswordResetToken validToken = allTokens.stream()
                .filter(token -> passwordEncoder.matches(rawToken, token.getHashedToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid password reset token."));
        Timestamp now = Timestamp.now();
        if (validToken.getExpiryDate().compareTo(now) < 0) {
            // Best practice: Delete the expired token from the database.
            tokenRepository.delete(validToken).block();
            throw new RuntimeException("Password reset token has expired.");
        }
        return validToken;
    }
}
