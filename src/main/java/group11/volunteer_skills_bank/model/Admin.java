package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "admins")
public class Admin {
    @DocumentId
    private String userId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role; // "superadmin" | "moderator" | "support"
    private Timestamp lastActive;
    private Timestamp createdAt;
}
