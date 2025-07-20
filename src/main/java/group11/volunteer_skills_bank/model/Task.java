package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;
import java.util.List;

@Data
@Document(collectionName = "tasks")
public class Task {
    @DocumentId
    private String taskId;
    
    private String opportunityId;
    private String title;
    private String description;
    private List<String> assignedTo; // user references
    private String status; // "pending" | "in-progress" | "completed" | "cancelled"
    private Timestamp dueDate;
    private Timestamp completedDate;
    private Double hoursLogged;
    private String notes;
    private String createdBy; // user reference
    private Timestamp createdAt;
}
