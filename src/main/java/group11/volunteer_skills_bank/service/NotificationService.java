package group11.volunteer_skills_bank.service;

import group11.volunteer_skills_bank.model.Notification;
import group11.volunteer_skills_bank.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public CompletableFuture<Notification> createIndividualApplicationNotification(
            String volunteerName, String postName, String organizationId, String recipientEmail) {
        
        String content = volunteerName + " is applied for " + postName;
        String title = "New Volunteer Application";
        
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setEmail(recipientEmail);
        notification.setRead(false);
        notification.setTimestamp(new Date());
        notification.setTitle(title);
        notification.setType("individual_application");
        notification.setVolunteerName(volunteerName);
        notification.setPostName(postName);
        notification.setOrganizationId(organizationId);
        
        return notificationRepository.save(notification)
            .thenApply(id -> notification);
    }

    public CompletableFuture<Notification> createTeamApplicationNotification(
            String teamName, String postName, List<String> teamMembers, 
            String organizationId, String recipientEmail) {
        
        String content = teamName + " is applied for " + postName + 
                         " with (" + teamMembers.size() + ") team members.";
        String title = "New Team Application";
        
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setEmail(recipientEmail);
        notification.setRead(false);
        notification.setTimestamp(new Date());
        notification.setTitle(title);
        notification.setType("team_application");
        notification.setTeamName(teamName);
        notification.setPostName(postName);
        notification.setTeamMembers(teamMembers);
        notification.setOrganizationId(organizationId);
        
        return notificationRepository.save(notification)
            .thenApply(id -> notification);
    }

    public CompletableFuture<Notification> createTeamRegistrationNotification(
            String volunteerName, String teamName, 
            String teamId, String leaderId, String recipientEmail) {
        
        String content = volunteerName + " is registered for " + teamName;
        String title = "New Team Registration";
        
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setEmail(recipientEmail);
        notification.setRead(false);
        notification.setTimestamp(new Date());
        notification.setTitle(title);
        notification.setType("team_registration");
        notification.setVolunteerName(volunteerName);
        notification.setTeamName(teamName);
        notification.setTeamId(teamId);
        notification.setLeaderId(leaderId);
        
        return notificationRepository.save(notification)
            .thenApply(id -> notification);
    }

    public CompletableFuture<List<Notification>> getApplicationNotificationsByOrganization(String organizationId) {
        return notificationRepository.findByOrganizationId(organizationId);
    }

    public CompletableFuture<List<Notification>> getNotificationsByTeamId(String teamId) {
        return notificationRepository.findByTeamId(teamId);
    }

    public CompletableFuture<Notification> markAsRead(String notificationId) {
        return notificationRepository.findByIdAndMarkRead(notificationId);
    }
}