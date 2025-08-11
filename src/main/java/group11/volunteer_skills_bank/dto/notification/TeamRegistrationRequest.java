package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class TeamRegistrationRequest {
    private String volunteerName;
    private String teamName;
    private String teamId;
    private String leaderId;
    private String recipientEmail;
}