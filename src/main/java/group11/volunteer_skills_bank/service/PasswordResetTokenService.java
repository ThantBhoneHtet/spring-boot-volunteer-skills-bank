package group11.volunteer_skills_bank.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.cloud.Timestamp;
import group11.volunteer_skills_bank.model.PasswordResetToken;
import group11.volunteer_skills_bank.model.Volunteer;
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
    
    public String createTokenForVolunteer(Volunteer volunteer) {
        // 1. Generate a secure, random, unique token
        String rawToken = UUID.randomUUID().toString();

        // 2. Hash the token before storing it in the database for security
        String hashedToken = passwordEncoder.encode(rawToken);

        // 3. Calculate the expiration date (15 minutes from now)
        Instant now = Instant.now();
        Instant expiration = now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);
        Timestamp expiryDate = Timestamp.of(java.sql.Timestamp.from(expiration));

        // 4. Create the token object to be saved
        PasswordResetToken resetToken = new PasswordResetToken(
                hashedToken,
                volunteer.getVolunteerId(),
                expiryDate);

        // 5. Save the token to the 'passwordResetTokens' collection in Firestore
        tokenRepository.save(resetToken).block(); // .block() waits for the async operation to complete

        // 6. Return the ORIGINAL, UNHASHED token to be included in the email link
        return rawToken;
    }
    
    public void resetPassword(String rawToken, String newPassword) {
        // 1. Find all tokens. We must do this because we can't query by a raw token.
        List<PasswordResetToken> allTokens = tokenRepository.findAll().collectList().block();
        if (allTokens == null || allTokens.isEmpty()) {
            throw new RuntimeException("Invalid password reset token.");
        }

        PasswordResetToken validToken = allTokens.stream()
                // 2. Find the token that matches the raw token when hashed
                .filter(token -> passwordEncoder.matches(rawToken, token.getHashedToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid password reset token."));

        // 3. Check if the token has expired
        Timestamp now = Timestamp.of(java.sql.Timestamp.from(Instant.now()));
        if (validToken.getExpiryDate().compareTo(now) < 0) {
            tokenRepository.delete(validToken).block(); // Clean up expired token
            throw new RuntimeException("Password reset token has expired.");
        }

        // 4. Get the associated volunteer
        Volunteer volunteer = volunteerService.getVolunteer(validToken.getVolunteerId()).block();
        if (volunteer == null) {
            throw new RuntimeException("User not found for the given token.");
        }

        // 5. Hash the new password and update the volunteer
        volunteer.setPassword(passwordEncoder.encode(newPassword));
        volunteerService.saveVolunteer(volunteer);

        // 6. Delete the token so it cannot be used again
        tokenRepository.delete(validToken).block();
    }
}
