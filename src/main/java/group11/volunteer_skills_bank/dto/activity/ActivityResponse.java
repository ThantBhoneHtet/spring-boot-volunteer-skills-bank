package group11.volunteer_skills_bank.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityResponse {
    private String activityId;
    private String type;
    private String action;
    private String userId;
    private String userName;
    private String adminId;
    private String adminName;
    private String details;
    private LocalDateTime timestamp;
    private String timeAgo;
    private Map<String, Object> metadata;
}
