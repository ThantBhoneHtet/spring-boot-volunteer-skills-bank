package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class IndividualApplicationRequestDTO {
    private String volunteerId;
    private String volunteerName;
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String recipientId;
}