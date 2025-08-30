package group11.volunteer_skills_bank.dto.notification;

import lombok.Data;
import java.util.List;

@Data
public class BaseNotificationDTO {
    private String senderId;
    private String senderName;
    private String senderType;
    private List<String> recipientIds;    // Changed to List
    private List<String> recipientNames;
    private String recipientType;
}