package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "reports")
public class Report {
    @DocumentId
    private String reportId;
    
    private String reporterId;
    private String reportedUserId; // optional
    private String reportedOpportunityId; // optional
    private String type; // "user" | "content" | "technical"
    private String reason;
    private String status; // "open" | "investigating" | "resolved"
    private Timestamp createdAt;
    private Timestamp resolvedAt;
    private String adminNotes;
}