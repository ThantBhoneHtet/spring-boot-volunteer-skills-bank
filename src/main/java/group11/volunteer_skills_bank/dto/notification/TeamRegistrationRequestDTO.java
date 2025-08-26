package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;
import java.util.List;
@Data
public class TeamRegistrationRequestDTO extends BaseNotificationDTO {
    private String teamId;
    private String teamName;
    private String volunteerId;
    private String volunteerName;
    private List<String> leaderIds;
}