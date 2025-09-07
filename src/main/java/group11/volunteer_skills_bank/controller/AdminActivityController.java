package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.activity.*;
import group11.volunteer_skills_bank.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/activities")
@CrossOrigin(origins = "*")
public class AdminActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * Fetches recent activities for the admin dashboard
     * GET /api/admin/activities/recent?limit=10&offset=0
     */
    @GetMapping("/recent")
    public ResponseEntity<Flux<ActivityResponse>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(activityService.getRecentActivities(limit, offset));
    }

    /**
     * Fetches activities by type
     * GET /api/admin/activities/type?type=user_registration&limit=10
     */
    @GetMapping("/type")
    public ResponseEntity<Flux<ActivityResponse>> getActivitiesByType(
            @RequestParam String type,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(activityService.getActivitiesByType(type, limit));
    }

    /**
     * Fetches activities for a specific user
     * GET /api/admin/activities/user/{userId}?limit=10
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Flux<ActivityResponse>> getUserActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(activityService.getUserActivities(userId, limit));
    }

    /**
     * Logs a new admin activity
     * POST /api/admin/activities/log
     */
    @PostMapping("/log")
    public ResponseEntity<Mono<ActivityResponse>> logActivity(@Valid @RequestBody ActivityLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.logActivity(request));
    }

    /**
     * Fetches activities with advanced filtering and pagination
     * GET
     * /api/admin/activities/filtered?type=user_registration&userId=123&dateFrom=2024-01-01T00:00:00&dateTo=2024-12-31T23:59:59&limit=10&offset=0
     */
    @GetMapping("/filtered")
    public ResponseEntity<Mono<FilteredActivitiesResponse>> getFilteredActivities(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        ActivityFilterRequest filters = new ActivityFilterRequest();
        filters.setType(type);
        filters.setUserId(userId);
        filters.setAdminId(adminId);
        filters.setLimit(limit);
        filters.setOffset(offset);

        // Parse date strings if provided
        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                // Handle ISO 8601 dates with timezone information
                java.time.Instant instant = java.time.Instant.parse(dateFrom);
                filters.setDateFrom(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                // Handle ISO 8601 dates with timezone information
                java.time.Instant instant = java.time.Instant.parse(dateTo);
                filters.setDateTo(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(activityService.getFilteredActivities(filters));
    }

    /**
     * Fetches activity statistics for dashboard
     * GET /api/admin/activities/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Mono<ActivityStatsResponse>> getActivityStats() {
        return ResponseEntity.ok(activityService.getActivityStats());
    }
}
