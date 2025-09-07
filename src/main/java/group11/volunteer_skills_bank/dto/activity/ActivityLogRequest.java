package group11.volunteer_skills_bank.dto.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLogRequest {
    @NotBlank(message = "Activity type is required")
    private String type;

    @NotBlank(message = "Action description is required")
    private String action;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String userName;

    @NotBlank(message = "Admin ID is required")
    private String adminId;

    private String adminName;

    private String details;

    private Map<String, Object> metadata;
}
