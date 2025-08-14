package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import lombok.Data;

@Data
public class Notification {
    private String id;
    private String title;
    private String type; // application, response, registration, registration_response, post_request, post_approval
    private boolean read;
    private Timestamp timestamp;
    private String content;
    private String recipientId;
    
    // Generic metadata fields (type-specific)
    private String applicantType; // "volunteer" or "team"
    private String applicantId;
    private String applicantName;
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String leaderId;
    private String volunteerId;
    private String volunteerName;
    private String teamId;
    private String teamName;
    private String status; // "accepted" or "rejected"
    private String adminId;
}