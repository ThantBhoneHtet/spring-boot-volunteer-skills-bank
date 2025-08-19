package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class TeamRegistrationRequestDTO {
    private String volunteerId;
    private String volunteerName;
    private String teamId;
    private String teamName;
    private String leaderId;
    private String recipientId;
}