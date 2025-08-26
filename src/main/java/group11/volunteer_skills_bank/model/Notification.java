package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import lombok.Data;
import java.util.List;
@Data
public class Notification {
    private String id;
    private String type; // "application_request", "team_registration_request", "post_request", 
                       // "application_response", "team_registration_response", "post_response"
    private String title;
    private String content;
    
    // Core participants
    private String senderId;
    private String senderName;
    private String senderType; // "volunteer", "team", "organization", "admin"
    private List<String> recipientIds;  // Changed to List<String>
    private List<String> recipientNames;
    private String recipientType; // "volunteer", "team_leader", "organization", "admin"
    
    // Reference entities
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String teamId;
    private String teamName;
    private String volunteerId;
    private String volunteerName;
    private List<String> leaderIds;
    private String adminId;
    
    // Status
    private boolean read;
    private String status; // "pending", "accepted", "rejected", "approved"
    private String responseMessage;
    private Timestamp timestamp;
}