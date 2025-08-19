package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.notification.*;
import group11.volunteer_skills_bank.model.Notification;
import group11.volunteer_skills_bank.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/individual-application")
    public String createIndividualApplicationNotification(
            @RequestBody IndividualApplicationRequestDTO request) throws ExecutionException, InterruptedException {
        return notificationService.createIndividualApplicationNotification(request);
    }

    @PostMapping("/team-application")
    public String createTeamApplicationNotification(
            @RequestBody TeamApplicationRequestDTO request) throws ExecutionException, InterruptedException {
        return notificationService.createTeamApplicationNotification(request);
    }

    @PostMapping("/team-registration")
    public String createTeamRegistrationNotification(
            @RequestBody TeamRegistrationRequestDTO request) throws ExecutionException, InterruptedException {
        return notificationService.createTeamRegistrationNotification(request);
    }

    // Application Response
    @PostMapping("/application-response")
    public String createApplicationResponse(
            @RequestBody ApplicationResponseDTO request) throws ExecutionException, InterruptedException {
        return notificationService.createApplicationResponse(request);
    }

    // Post Request
    @PostMapping("/post-request")
    public String createPostRequest(
            @RequestBody PostRequestDTO request) throws ExecutionException, InterruptedException {
        return notificationService.createPostRequest(request);
    }

    // Post Response
    @PostMapping("/post-response")
    public String createPostResponse(
            @RequestBody PostResponseDTO request) throws ExecutionException, InterruptedException {
        return notificationService.createPostResponse(request);
    }

    @GetMapping("/recipient/{recipientId}")
    public List<Notification> getNotificationsByRecipient(
            @PathVariable String recipientId) throws ExecutionException, InterruptedException {
        return notificationService.getNotificationsByRecipient(recipientId);
    }

    @PutMapping("/{notificationId}/mark-read")
    public void markNotificationAsRead(
            @PathVariable String notificationId) throws ExecutionException, InterruptedException {
        notificationService.markNotificationAsRead(notificationId);
    }
}