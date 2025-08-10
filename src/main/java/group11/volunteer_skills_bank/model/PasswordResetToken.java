package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "passwordResetTokens")
public class PasswordResetToken {

    /**
     * The hashed version of the token. Never store the raw token.
     */
    @DocumentId
    private String hashedToken;

    /**
     * The ID of the volunteer this token belongs to.
     */
    private String userId;

    private String userType;

    private Timestamp expiryDate;
}