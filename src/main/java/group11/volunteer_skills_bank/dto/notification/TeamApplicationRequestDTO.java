package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class TeamApplicationRequestDTO {
    private String teamId;
    private String teamName;
    private String leaderId;
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String recipientId;
}