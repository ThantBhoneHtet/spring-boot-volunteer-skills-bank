package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class PostResponseDTO {
    private String organizationId;
    private String organizationName;
    private String opportunityId;
    private String opportunityName;
    private String recipientId; // orgId
    private String status; // "approved" or "rejected"
    private String adminId; // Who responded
}