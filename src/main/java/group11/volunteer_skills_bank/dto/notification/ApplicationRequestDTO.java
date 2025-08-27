package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;
import java.util.List;

@Data
public class ApplicationRequestDTO extends BaseNotificationDTO {
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String volunteerId;
    private String volunteerName;
    private String teamId;
    private String teamName;
    private List<String> leaderIds;
}