package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;

@Data
public class PostRequestDTO extends BaseNotificationDTO {
    private String opportunityId;
    private String opportunityName;
    private String organizationId;
    private String organizationName;
    private String adminId;
}