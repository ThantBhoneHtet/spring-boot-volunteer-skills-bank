package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "notifications")
public class Notification {
    @DocumentId
    private String notificationId;
    
    private String userId;
    private String title;
    private String content;
    private String relatedEntityId; // reference to opportunity/application/etc.
    private Boolean read;
    private Timestamp timestamp;
    private String actionUrl;
}
