package group11.volunteer_skills_bank.service;

import com.google.cloud.Timestamp;
import group11.volunteer_skills_bank.dto.activity.*;
import group11.volunteer_skills_bank.model.Activity;
import group11.volunteer_skills_bank.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * Log a new admin activity
     */
    public Mono<ActivityResponse> logActivity(ActivityLogRequest request) {
        Activity activity = new Activity();
        activity.setType(request.getType());
        activity.setAction(request.getAction());
        activity.setUserId(request.getUserId());
        activity.setUserName(request.getUserName());
        activity.setAdminId(request.getAdminId());
        activity.setAdminName(request.getAdminName());
        activity.setDetails(request.getDetails());
        activity.setTimestamp(Timestamp.now());
        activity.setMetadata(request.getMetadata());

        return activityRepository.save(activity)
                .map(this::convertToResponse);
    }

    /**
     * Get recent activities with pagination
     */
    public Flux<ActivityResponse> getRecentActivities(int limit, int offset) {
        return activityRepository.findAll()
                .sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .skip(offset)
                .take(limit)
                .map(this::convertToResponse);
    }

    /**
     * Get activities by type
     */
    public Flux<ActivityResponse> getActivitiesByType(String type, int limit) {
        return activityRepository.findByType(type)
                .sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .take(limit)
                .map(this::convertToResponse);
    }

    /**
     * Get activities for a specific user
     */
    public Flux<ActivityResponse> getUserActivities(String userId, int limit) {
        return activityRepository.findByUserId(userId)
                .sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .take(limit)
                .map(this::convertToResponse);
    }

    /**
     * Get filtered activities with advanced filtering and pagination
     */
    public Mono<FilteredActivitiesResponse> getFilteredActivities(ActivityFilterRequest filters) {
        Flux<Activity> activitiesFlux = activityRepository.findAll();

        // Apply filters
        if (filters.getType() != null && !filters.getType().isEmpty()) {
            activitiesFlux = activitiesFlux.filter(activity -> activity.getType().equals(filters.getType()));
        }

        if (filters.getUserId() != null && !filters.getUserId().isEmpty()) {
            activitiesFlux = activitiesFlux.filter(activity -> activity.getUserId().equals(filters.getUserId()));
        }

        if (filters.getAdminId() != null && !filters.getAdminId().isEmpty()) {
            activitiesFlux = activitiesFlux.filter(activity -> activity.getAdminId().equals(filters.getAdminId()));
        }

        if (filters.getDateFrom() != null) {
            Timestamp fromTimestamp = Timestamp.of(java.sql.Timestamp.valueOf(filters.getDateFrom()));
            activitiesFlux = activitiesFlux.filter(activity -> activity.getTimestamp().compareTo(fromTimestamp) >= 0);
        }

        if (filters.getDateTo() != null) {
            Timestamp toTimestamp = Timestamp.of(java.sql.Timestamp.valueOf(filters.getDateTo()));
            activitiesFlux = activitiesFlux.filter(activity -> activity.getTimestamp().compareTo(toTimestamp) <= 0);
        }

        // Sort by timestamp descending
        activitiesFlux = activitiesFlux.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // Get total count
        Mono<Long> totalCount = activitiesFlux.count();

        // Apply pagination
        Flux<Activity> paginatedActivities = activitiesFlux
                .skip(filters.getOffset())
                .take(filters.getLimit());

        return Mono.zip(
                paginatedActivities.map(this::convertToResponse).collectList(),
                totalCount).map(tuple -> {
                    List<ActivityResponse> activities = tuple.getT1();
                    Long total = tuple.getT2();
                    boolean hasMore = (filters.getOffset() + filters.getLimit()) < total;

                    return new FilteredActivitiesResponse(activities, total, hasMore, filters.getLimit(),
                            filters.getOffset());
                });
    }

    /**
     * Get activity statistics for dashboard
     */
    public Mono<ActivityStatsResponse> getActivityStats() {
        Timestamp now = Timestamp.now();
        Timestamp todayStart = Timestamp.of(java.sql.Timestamp.valueOf(
                LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)));
        Timestamp weekStart = Timestamp.of(java.sql.Timestamp.valueOf(
                LocalDateTime.now().minusWeeks(1)));
        Timestamp monthStart = Timestamp.of(java.sql.Timestamp.valueOf(
                LocalDateTime.now().minusMonths(1)));

        return Mono.zip(
                activityRepository.count(),
                countActivitiesInDateRange(todayStart, now),
                countActivitiesInDateRange(weekStart, now),
                countActivitiesInDateRange(monthStart, now)).flatMap(tuple -> {
                    Long totalActivities = tuple.getT1();
                    Long todayActivities = tuple.getT2();
                    Long weeklyActivities = tuple.getT3();
                    Long monthlyActivities = tuple.getT4();

                    // Get activity type counts
                    return getActivityTypeCounts().map(activityTypes -> new ActivityStatsResponse(totalActivities,
                            todayActivities, weeklyActivities, monthlyActivities, activityTypes));
                });
    }

    /**
     * Count activities within a date range
     */
    private Mono<Long> countActivitiesInDateRange(Timestamp startDate, Timestamp endDate) {
        return activityRepository.findAll()
                .filter(activity -> activity.getTimestamp().compareTo(startDate) >= 0
                        && activity.getTimestamp().compareTo(endDate) <= 0)
                .count();
    }

    /**
     * Get counts for each activity type
     */
    private Mono<Map<String, Long>> getActivityTypeCounts() {
        return Flux.fromArray(new String[] {
                Activity.USER_REGISTRATION,
                Activity.POST_APPROVAL,
                Activity.POST_REJECTION,
                Activity.POST_DELETION,
                Activity.ORGANIZATION_APPROVAL,
                Activity.ORGANIZATION_REJECTION,
                Activity.USER_SUSPENSION,
                Activity.USER_REACTIVATION,
                Activity.GROUP_CREATION,
                Activity.SKILL_ADDITION,
                Activity.REPORT_RESOLVED
        })
                .flatMap(type -> activityRepository.countByType(type)
                        .map(count -> Map.entry(type, count)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(map -> {
                    // Ensure all types are present with 0 count if not found
                    for (String type : new String[] {
                            Activity.USER_REGISTRATION,
                            Activity.POST_APPROVAL,
                            Activity.POST_REJECTION,
                            Activity.POST_DELETION,
                            Activity.ORGANIZATION_APPROVAL,
                            Activity.ORGANIZATION_REJECTION,
                            Activity.USER_SUSPENSION,
                            Activity.USER_REACTIVATION,
                            Activity.GROUP_CREATION,
                            Activity.SKILL_ADDITION,
                            Activity.REPORT_RESOLVED
                    }) {
                        map.putIfAbsent(type, 0L);
                    }
                    return map;
                });
    }

    /**
     * Convert Activity entity to ActivityResponse DTO
     */
    private ActivityResponse convertToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setActivityId(activity.getActivityId());
        response.setType(activity.getType());
        response.setAction(activity.getAction());
        response.setUserId(activity.getUserId());
        response.setUserName(activity.getUserName());
        response.setAdminId(activity.getAdminId());
        response.setAdminName(activity.getAdminName());
        response.setDetails(activity.getDetails());
        response.setMetadata(activity.getMetadata());

        // Convert Timestamp to LocalDateTime
        LocalDateTime timestamp = LocalDateTime.ofInstant(
                activity.getTimestamp().toDate().toInstant(),
                ZoneId.systemDefault());
        response.setTimestamp(timestamp);
        response.setTimeAgo(formatTimeAgo(timestamp));

        return response;
    }

    /**
     * Format timestamp to human-readable "time ago" string
     */
    private String formatTimeAgo(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(timestamp, now);

        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "") + " ago";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else {
            long days = seconds / 86400;
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        }
    }
}
