package group11.volunteer_skills_bank.dto.notification;


import lombok.Data;

@Data
public class IndividualApplicationRequest {
    private String volunteerName;
    private String postName;
    private String organizationId;
    private String recipientEmail;
}