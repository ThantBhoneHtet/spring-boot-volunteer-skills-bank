package group11.volunteer_skills_bank.dto.notification;
import lombok.Data;

@Data
public class TeamRegistrationResponseDTO extends BaseNotificationDTO {
    private String teamId;
    private String teamName;
    private String volunteerId;
    private String volunteerName;
    private String leaderId;
    private String status;
    private String responseMessage;
}
