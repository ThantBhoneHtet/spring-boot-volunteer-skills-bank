package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;
import java.util.List;

@Data
public class TeamApplicationRequest {
    private String teamName;
    private String postName;
    private List<String> teamMembers;
    private String organizationId;
    private String recipientEmail;
}