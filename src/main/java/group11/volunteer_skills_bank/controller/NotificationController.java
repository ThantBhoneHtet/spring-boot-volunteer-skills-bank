package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.notification.*;
import group11.volunteer_skills_bank.model.Notification;
import group11.volunteer_skills_bank.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/application-request")
    public String createApplicationRequest(@RequestBody ApplicationRequestDTO request) 
            throws ExecutionException, InterruptedException {
        
        System.out.println("Received ApplicationRequestDTO:");
        System.out.println("Team Name: " + request.getTeamName());
        System.out.println("Volunteer Name: " + request.getVolunteerName());
        System.out.println("Opportunity Name: " + request.getOpportunityName());
        
        try {
            return notificationService.createApplicationRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/team-registration-request")
    public String createTeamRegistrationRequest(@RequestBody TeamRegistrationRequestDTO request) 
            throws ExecutionException, InterruptedException {
        return notificationService.createTeamRegistrationRequest(request);
    }

    @PostMapping("/post-request")
    public String createPostRequest(@RequestBody PostRequestDTO request) 
            throws ExecutionException, InterruptedException {
        return notificationService.createPostRequest(request);
    }

    // RESPONSE ENDPOINTS
    @PostMapping("/application-response")
    public String createApplicationResponse(@RequestBody ApplicationResponseDTO request) 
            throws ExecutionException, InterruptedException {
        return notificationService.createApplicationResponse(request);
    }

    @PostMapping("/team-registration-response")
    public String createTeamRegistrationResponse(@RequestBody TeamRegistrationResponseDTO request) 
            throws ExecutionException, InterruptedException {
        return notificationService.createTeamRegistrationResponse(request);
    }

    @PostMapping("/post-response")
    public String createPostResponse(@RequestBody PostResponseDTO request) 
            throws ExecutionException, InterruptedException {
        return notificationService.createPostResponse(request);
    }

    // REJECTION ENDPOINTS
    @PostMapping("/application/reject")
    public String rejectApplication(@RequestBody RejectionRequestDTO rejectionRequest) 
            throws ExecutionException, InterruptedException {
        return notificationService.rejectApplication(rejectionRequest);
    }

    @PostMapping("/team-registration/reject")
    public String rejectTeamRegistration(@RequestBody RejectionRequestDTO rejectionRequest) 
            throws ExecutionException, InterruptedException {
        return notificationService.rejectTeamRegistration(rejectionRequest);
    }

    @PostMapping("/post/reject")
    public String rejectPost(@RequestBody RejectionRequestDTO rejectionRequest) 
            throws ExecutionException, InterruptedException {
        return notificationService.rejectPost(rejectionRequest);
    }

    // GET ENDPOINTS
    @GetMapping("/recipient/{recipientId}")
    public List<Notification> getNotificationsByRecipient(@PathVariable String recipientId) 
            throws ExecutionException, InterruptedException {
        return notificationService.getNotificationsByRecipient(recipientId);
    }

    @GetMapping("/recipient/{recipientId}/unread")
    public List<Notification> getUnreadNotificationsByRecipient(@PathVariable String recipientId) 
            throws ExecutionException, InterruptedException {
        return notificationService.getUnreadNotificationsByRecipient(recipientId);
    }

    @GetMapping("/{notificationId}")
    public Notification getNotificationById(@PathVariable String notificationId) 
            throws ExecutionException, InterruptedException {
        return notificationService.getNotificationById(notificationId);
    }

    // New endpoint to get notifications with filtering
    @GetMapping
    public List<Notification> getNotifications(
            @RequestParam String recipientId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) String priority) 
            throws ExecutionException, InterruptedException {
        
        return notificationService.getNotificationsWithFilters(recipientId, type, unreadOnly, priority);
    }

    @PutMapping("/{notificationId}/mark-read")
    public void markNotificationAsRead(@PathVariable String notificationId) 
            throws ExecutionException, InterruptedException {
        notificationService.markNotificationAsRead(notificationId);
    }

    @GetMapping("/recipient/{recipientId}/formatted")
    public List<Map<String, Object>> getFormattedNotificationsByRecipient(@PathVariable String recipientId) 
            throws ExecutionException, InterruptedException {
        List<Notification> notifications = notificationService.getNotificationsByRecipient(recipientId);
        return notifications.stream()
                .map(notificationService::formatNotificationForUI)
                .collect(Collectors.toList());
    }


    // New endpoint to mark all as read
    @PutMapping("/mark-all-read")
    public void markAllAsRead(@RequestParam String recipientId) 
            throws ExecutionException, InterruptedException {
        notificationService.markAllAsRead(recipientId);
    }
}