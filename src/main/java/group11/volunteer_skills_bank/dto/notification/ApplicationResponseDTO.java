package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class ApplicationResponseDTO {
    private String applicantId; // volunteerId or teamId
    private String applicantType; // "volunteer" or "team"
    private String applicantName;
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String recipientId; // volunteerId or leaderId
    private String status; // "accepted" or "rejected"
    private String leaderId; // Only for team applications
}