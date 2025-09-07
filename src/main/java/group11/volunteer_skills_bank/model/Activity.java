package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collectionName = "admin_activities")
public class Activity {
    @DocumentId
    private String activityId;

    private String type; // user_registration, post_approval, post_rejection, etc.
    private String action; // Human-readable action description
    private String userId; // ID of the user involved in the activity
    private String userName; // Name of the user involved
    private String adminId; // ID of the admin who performed the action
    private String adminName; // Name of the admin who performed the action
    private String details; // Detailed description of the activity
    private Timestamp timestamp;
    private Map<String, Object> metadata; // Additional metadata like IP address, user agent, etc.

    // Activity type constants
    public static final String USER_REGISTRATION = "user_registration";
    public static final String POST_APPROVAL = "post_approval";
    public static final String POST_REJECTION = "post_rejection";
    public static final String POST_DELETION = "post_deletion";
    public static final String ORGANIZATION_APPROVAL = "organization_approval";
    public static final String ORGANIZATION_REJECTION = "organization_rejection";
    public static final String USER_SUSPENSION = "user_suspension";
    public static final String USER_REACTIVATION = "user_reactivation";
    public static final String GROUP_CREATION = "group_creation";
    public static final String SKILL_ADDITION = "skill_addition";
    public static final String REPORT_RESOLVED = "report_resolved";
}
