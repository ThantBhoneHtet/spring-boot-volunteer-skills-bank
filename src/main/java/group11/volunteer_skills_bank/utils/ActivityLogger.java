package group11.volunteer_skills_bank.utils;

import group11.volunteer_skills_bank.dto.activity.ActivityLogRequest;
import group11.volunteer_skills_bank.model.Activity;
import group11.volunteer_skills_bank.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utility class for logging admin activities throughout the application
 */
@Component
public class ActivityLogger {

    @Autowired
    private ActivityService activityService;

    /**
     * Log a user registration activity
     */
    public void logUserRegistration(String userId, String userName, String adminId, String adminName) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.USER_REGISTRATION);
        request.setAction("New volunteer registration");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(userName + " completed their volunteer registration");

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a post approval activity
     */
    public void logPostApproval(String userId, String userName, String adminId, String adminName, String postTitle) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.POST_APPROVAL);
        request.setAction("Post approved");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails("Post '" + postTitle + "' by " + userName + " has been approved and published");

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a post rejection activity
     */
    public void logPostRejection(String userId, String userName, String adminId, String adminName, String postTitle,
            String reason) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.POST_REJECTION);
        request.setAction("Post rejected");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails("Post '" + postTitle + "' by " + userName + " was rejected due to: " + reason);

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a post deletion activity
     */
    public void logPostDeletion(String userId, String userName, String adminId, String adminName, String postTitle,
            String reason) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.POST_DELETION);
        request.setAction("Post deleted");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails("Post '" + postTitle + "' by " + userName + " was deleted by admin. Reason: " + reason);

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log an organization approval activity
     */
    public void logOrganizationApproval(String organizationId, String organizationName, String adminId,
            String adminName) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.ORGANIZATION_APPROVAL);
        request.setAction("Organization approved");
        request.setUserId(organizationId);
        request.setUserName(organizationName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(organizationName + " organization has been verified and approved");

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log an organization rejection activity
     */
    public void logOrganizationRejection(String organizationId, String organizationName, String adminId,
            String adminName, String reason) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.ORGANIZATION_REJECTION);
        request.setAction("Organization rejected");
        request.setUserId(organizationId);
        request.setUserName(organizationName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(organizationName + " organization request was rejected. Reason: " + reason);

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a user suspension activity
     */
    public void logUserSuspension(String userId, String userName, String adminId, String adminName, int days,
            String reason) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.USER_SUSPENSION);
        request.setAction("User suspended");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(userName + " account has been suspended for " + days + " days. Reason: " + reason);

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a user reactivation activity
     */
    public void logUserReactivation(String userId, String userName, String adminId, String adminName) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.USER_REACTIVATION);
        request.setAction("User reactivated");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(userName + " account has been reactivated");

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a group creation activity
     */
    public void logGroupCreation(String userId, String userName, String adminId, String adminName, String groupName) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.GROUP_CREATION);
        request.setAction("Group created");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(userName + " created a new volunteer group: " + groupName);

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a skill addition activity
     */
    public void logSkillAddition(String userId, String userName, String adminId, String adminName, String skillName) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.SKILL_ADDITION);
        request.setAction("New skill added");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails("New skill category '" + skillName + "' added by " + userName);

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a report resolution activity
     */
    public void logReportResolved(String userId, String userName, String adminId, String adminName, String reportId) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(Activity.REPORT_RESOLVED);
        request.setAction("Report resolved");
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails("Report #" + reportId + " against " + userName + " has been resolved");

        activityService.logActivity(request).subscribe();
    }

    /**
     * Log a custom activity
     */
    public void logCustomActivity(String type, String action, String userId, String userName,
            String adminId, String adminName, String details, Map<String, Object> metadata) {
        ActivityLogRequest request = new ActivityLogRequest();
        request.setType(type);
        request.setAction(action);
        request.setUserId(userId);
        request.setUserName(userName);
        request.setAdminId(adminId);
        request.setAdminName(adminName);
        request.setDetails(details);
        request.setMetadata(metadata);

        activityService.logActivity(request).subscribe();
    }
}
