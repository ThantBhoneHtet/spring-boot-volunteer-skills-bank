package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "applications")
public class Application {
    @DocumentId
    private String applicationId;
    
    private String opportunityId;
    private String volunteerId;
    private String status; // "pending" | "approved" | "rejected" | "withdrawn"
    private Timestamp applicationDate;
}
