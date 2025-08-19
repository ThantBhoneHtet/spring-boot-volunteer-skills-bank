package group11.volunteer_skills_bank.service;

import com.google.cloud.Timestamp;
import group11.volunteer_skills_bank.dto.notification.*;
import group11.volunteer_skills_bank.model.Notification;
import group11.volunteer_skills_bank.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public String createIndividualApplicationNotification(IndividualApplicationRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("application");
        notification.setTitle("New Volunteer Application");
        notification.setContent(dto.getVolunteerName() + " applied for " + dto.getOpportunityName());
        notification.setRecipientId(dto.getRecipientId());
        notification.setTimestamp(Timestamp.now());
        notification.setRead(false);
        
        // Set metadata
        notification.setApplicantType("volunteer");
        notification.setApplicantId(dto.getVolunteerId());
        notification.setApplicantName(dto.getVolunteerName());
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        
        return notificationRepository.save(notification);
    }

    public String createTeamApplicationNotification(TeamApplicationRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("application");
        notification.setTitle("New Team Application");
        notification.setContent(dto.getTeamName() + " applied for " + dto.getOpportunityName());
        notification.setRecipientId(dto.getRecipientId());
        notification.setTimestamp(Timestamp.now());
        notification.setRead(false);
        
        // Set metadata
        notification.setApplicantType("team");
        notification.setApplicantId(dto.getTeamId());
        notification.setApplicantName(dto.getTeamName());
        notification.setLeaderId(dto.getLeaderId());
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        
        return notificationRepository.save(notification);
    }

    public String createTeamRegistrationNotification(TeamRegistrationRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("registration");
        notification.setTitle("New Team Registration Request");
        notification.setContent(dto.getVolunteerName() + " requested to join " + dto.getTeamName());
        notification.setRecipientId(dto.getRecipientId());
        notification.setTimestamp(Timestamp.now());
        notification.setRead(false);
        
        // Set metadata
        notification.setVolunteerId(dto.getVolunteerId());
        notification.setVolunteerName(dto.getVolunteerName());
        notification.setTeamId(dto.getTeamId());
        notification.setTeamName(dto.getTeamName());
        notification.setLeaderId(dto.getLeaderId());
        
        return notificationRepository.save(notification);
    }

    // Application Response (Org → Volunteer/Team Leader)
    public String createApplicationResponse(ApplicationResponseDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("application_response");
        notification.setTitle("Application Decision");
        notification.setContent(String.format("%s %s your application for %s",
                dto.getOrganizationName(),
                dto.getStatus(),
                dto.getOpportunityName()));
        notification.setRecipientId(dto.getRecipientId());
        notification.setTimestamp(Timestamp.now());
        
        // Set metadata
        notification.setApplicantType(dto.getApplicantType());
        notification.setApplicantId(dto.getApplicantId());
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setStatus(dto.getStatus());
        
        if("team".equals(dto.getApplicantType())) {
            notification.setLeaderId(dto.getLeaderId());
        }
        
        return notificationRepository.save(notification);
    }

    // Post Request (Org → Admin)
    public String createPostRequest(PostRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("post_request");
        notification.setTitle("New Post Approval Request");
        notification.setContent(String.format("%s requested approval for %s",
                dto.getOrganizationName(),
                dto.getOpportunityName()));
        notification.setRecipientId(dto.getAdminId());
        notification.setTimestamp(Timestamp.now());
        
        // Set metadata
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        
        return notificationRepository.save(notification);
    }

    // Post Response (Admin → Org)
    public String createPostResponse(PostResponseDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("post_response");
        notification.setTitle("Post Approval Decision");
        notification.setContent(String.format("Your post %s has been %s",
                dto.getOpportunityName(),
                dto.getStatus()));
        notification.setRecipientId(dto.getRecipientId());
        notification.setTimestamp(Timestamp.now());
        
        // Set metadata
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setStatus(dto.getStatus());
        notification.setAdminId(dto.getAdminId());
        
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByRecipient(String recipientId) throws ExecutionException, InterruptedException {
        return notificationRepository.findByRecipientId(recipientId);
    }

    public void markNotificationAsRead(String notificationId) throws ExecutionException, InterruptedException {
        notificationRepository.markAsRead(notificationId);
    }
}