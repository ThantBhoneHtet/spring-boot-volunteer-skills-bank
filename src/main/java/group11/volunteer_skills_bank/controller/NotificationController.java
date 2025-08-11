package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.notification.IndividualApplicationRequest;
import group11.volunteer_skills_bank.dto.notification.TeamApplicationRequest;
import group11.volunteer_skills_bank.dto.notification.TeamRegistrationRequest;
import group11.volunteer_skills_bank.model.Notification;
import group11.volunteer_skills_bank.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PostMapping("/individual-application")
        public CompletableFuture<Notification> createIndividualApplicationNotification(
            @RequestBody IndividualApplicationRequest request) {
        return notificationService.createIndividualApplicationNotification(
            request.getVolunteerName(), 
            request.getPostName(), 
            request.getOrganizationId(), 
            request.getRecipientEmail());
    }

    @PostMapping("/team-application")
    public CompletableFuture<Notification> createTeamApplicationNotification(
            @RequestBody TeamApplicationRequest request) {
        return notificationService.createTeamApplicationNotification(
            request.getTeamName(),
            request.getPostName(),
            request.getTeamMembers(),
            request.getOrganizationId(),
            request.getRecipientEmail());
    }

    @PostMapping("/team-registration")
    public CompletableFuture<Notification> createTeamRegistrationNotification(
            @RequestBody TeamRegistrationRequest request) {
        return notificationService.createTeamRegistrationNotification(
            request.getVolunteerName(),
            request.getTeamName(),
            request.getTeamId(),
            request.getLeaderId(),
            request.getRecipientEmail());
    }

    @GetMapping("/organization/{organizationId}")
    public CompletableFuture<List<Notification>> getApplicationNotificationsByOrganization(
            @PathVariable String organizationId) {
        return notificationService.getApplicationNotificationsByOrganization(organizationId);
    }

    @GetMapping("/team/{teamId}")
    public CompletableFuture<List<Notification>> getNotificationsByTeamId(
            @PathVariable String teamId) {
        return notificationService.getNotificationsByTeamId(teamId);
    }

    @PutMapping("/{notificationId}/mark-read")
    public CompletableFuture<Notification> markNotificationAsRead(
            @PathVariable String notificationId) {
        return notificationService.markAsRead(notificationId);
    }
}