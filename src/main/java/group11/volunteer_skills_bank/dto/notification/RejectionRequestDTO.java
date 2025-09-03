package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;
import java.util.List;

@Data
public class RejectionRequestDTO {
    private String reason; // Rejection reason from input
    
    // Sender info (the one rejecting)
    private String senderId;
    private String senderName;
    private String senderType;
    
    // Recipient info (the one being rejected)
    private List<String> recipientIds;
    private List<String> recipientNames;
    private String recipientType;
    
    // Additional context based on notification type
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String volunteerId;
    private String volunteerName;
    private String teamId;
    private String teamName;
    private List<String> leaderIds;
    private String adminId;
    
    private String notificationType; // "application_request", "team_registration_request", "post_request"
}