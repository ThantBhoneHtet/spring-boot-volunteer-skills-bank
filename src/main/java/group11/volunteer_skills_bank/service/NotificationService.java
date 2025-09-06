package group11.volunteer_skills_bank.service;

import group11.volunteer_skills_bank.dto.notification.*;
import group11.volunteer_skills_bank.model.Notification;
import group11.volunteer_skills_bank.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.google.cloud.Timestamp;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // REQUEST NOTIFICATIONS
    public String createApplicationRequest(ApplicationRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("application_request");
        
        // In createApplicationRequest method:
        if (dto.getTeamName() != null && !dto.getTeamName().isEmpty()) {
            notification.setTitle("New Team Application");
            notification.setContent(String.format("%s applied for %s", dto.getTeamName(), dto.getOpportunityName()));
        } else {
            notification.setTitle("New Volunteer Application");
            notification.setContent(String.format("%s applied for %s", 
                dto.getVolunteerName() != null ? dto.getVolunteerName() : "A volunteer", 
                dto.getOpportunityName()));
        }
        
        setBaseFields(notification, dto);
        setApplicationRequestFields(notification, dto);
        notification.setStatus("pending");
        
        return notificationRepository.save(notification);
    }

    public String createTeamRegistrationRequest(TeamRegistrationRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("team_registration_request");
        notification.setTitle("Team Join Request");
        notification.setContent(String.format("%s requested to join %s", dto.getVolunteerName(), dto.getTeamName()));
        notification.setStatus("pending");
        
        setBaseFields(notification, dto);
        notification.setTeamId(dto.getTeamId());
        notification.setTeamName(dto.getTeamName());
        notification.setVolunteerId(dto.getVolunteerId());
        notification.setVolunteerName(dto.getVolunteerName());
        notification.setLeaderIds(dto.getLeaderIds());
        
        return notificationRepository.save(notification);
    }

    public String createPostRequest(PostRequestDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("post_request");
        notification.setTitle("Post Approval Request");
        notification.setContent(String.format("%s requested approval for %s", dto.getOrganizationName(), dto.getOpportunityName()));
        notification.setStatus("pending");
        
        setBaseFields(notification, dto);
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        notification.setAdminId(dto.getAdminId());
        
        return notificationRepository.save(notification);
    }

    // RESPONSE NOTIFICATIONS
    public String createApplicationResponse(ApplicationResponseDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("application_response");
        notification.setTitle("Application Decision");
        notification.setContent(String.format("%s %s your application for %s", 
                dto.getOrganizationName(), dto.getStatus(), dto.getOpportunityName()));
        notification.setStatus(dto.getStatus());
        
        setBaseFields(notification, dto);
        setApplicationResponseFields(notification, dto);
        
        return notificationRepository.save(notification);
    }

    public String createTeamRegistrationResponse(TeamRegistrationResponseDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("team_registration_response");
        notification.setTitle("Team Join Decision");
        notification.setContent(String.format("You have been %s to join %s", dto.getStatus(), dto.getTeamName()));
        notification.setStatus(dto.getStatus());
        
        setBaseFields(notification, dto);
        notification.setTeamId(dto.getTeamId());
        notification.setTeamName(dto.getTeamName());
        notification.setVolunteerId(dto.getVolunteerId());
        notification.setVolunteerName(dto.getVolunteerName());
        notification.setLeaderIds(List.of(dto.getLeaderId()));
        
        return notificationRepository.save(notification);
    }
    public String createPostResponse(PostResponseDTO dto) throws ExecutionException, InterruptedException {
        Notification notification = new Notification();
        notification.setType("post_response");
        notification.setTitle("Post Approval Decision");
        notification.setContent(String.format("Admin %s your post %s", dto.getStatus(), dto.getOpportunityName()));
        notification.setStatus(dto.getStatus());
        
        // Set base fields manually to handle recipientIds array
        notification.setSenderId(dto.getSenderId());
        notification.setSenderName(dto.getSenderName());
        notification.setSenderType(dto.getSenderType());
        notification.setRecipientIds(dto.getRecipientIds()); // This should be a List<String>
        notification.setRecipientNames(dto.getRecipientNames()); // This should be a List<String>
        notification.setRecipientType(dto.getRecipientType());
        
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        notification.setAdminId(dto.getAdminId());
        notification.setResponseMessage(dto.getResponseMessage());
        
        // Set timestamp
        notification.setTimestamp(Timestamp.now());
        
        return notificationRepository.save(notification);
    }

    // REJECTION METHODS (NO original notification lookup needed)
    public String rejectApplication(RejectionRequestDTO rejectionRequest) 
            throws ExecutionException, InterruptedException {
        
        ApplicationResponseDTO responseDTO = new ApplicationResponseDTO();
        
        // Set sender (the one rejecting)
        responseDTO.setSenderId(rejectionRequest.getSenderId());
        responseDTO.setSenderName(rejectionRequest.getSenderName());
        responseDTO.setSenderType(rejectionRequest.getSenderType());
        
        // Set recipient (the one being rejected)
        responseDTO.setRecipientIds(rejectionRequest.getRecipientIds());
        responseDTO.setRecipientNames(rejectionRequest.getRecipientNames());
        responseDTO.setRecipientType(rejectionRequest.getRecipientType());
        
        // Set entity references
        responseDTO.setOpportunityId(rejectionRequest.getOpportunityId());
        responseDTO.setOpportunityName(rejectionRequest.getOpportunityName());
        responseDTO.setOrganizationId(rejectionRequest.getOrganizationId());
        responseDTO.setOrganizationName(rejectionRequest.getOrganizationName());
        responseDTO.setVolunteerId(rejectionRequest.getVolunteerId());
        responseDTO.setVolunteerName(rejectionRequest.getVolunteerName());
        responseDTO.setTeamId(rejectionRequest.getTeamId());
        responseDTO.setTeamName(rejectionRequest.getTeamName());
        responseDTO.setLeaderIds(rejectionRequest.getLeaderIds());
        
        // Set rejection status and reason
        responseDTO.setStatus("rejected");
        responseDTO.setResponseMessage(rejectionRequest.getReason());

        return createApplicationResponse(responseDTO);
    }

    public String rejectTeamRegistration(RejectionRequestDTO rejectionRequest) 
    throws ExecutionException, InterruptedException {

        // Create a Notification directly, NOT a TeamRegistrationResponseDTO
        Notification notification = new Notification();
        notification.setType("team_registration_response");
        notification.setTitle("Team Join Decision");
        notification.setContent(String.format("You have been rejected from joining %s", rejectionRequest.getTeamName()));

        // Set base fields
        notification.setSenderId(rejectionRequest.getSenderId());
        notification.setSenderName(rejectionRequest.getSenderName());
        notification.setSenderType(rejectionRequest.getSenderType());
        notification.setRecipientIds(rejectionRequest.getRecipientIds());
        notification.setRecipientNames(rejectionRequest.getRecipientNames());
        notification.setRecipientType(rejectionRequest.getRecipientType());

        // Set entity references
        notification.setTeamId(rejectionRequest.getTeamId());
        notification.setTeamName(rejectionRequest.getTeamName());
        notification.setVolunteerId(rejectionRequest.getVolunteerId());
        notification.setVolunteerName(rejectionRequest.getVolunteerName());
        notification.setLeaderIds(rejectionRequest.getLeaderIds());

        // Set rejection status and reason - THIS IS THE KEY!
        notification.setStatus("rejected");
        notification.setResponseMessage(rejectionRequest.getReason()); // ← This should work now

        notification.setTimestamp(com.google.cloud.Timestamp.now());
        notification.setRead(false);

        return notificationRepository.save(notification);
    }
    public String rejectPost(RejectionRequestDTO rejectionRequest) 
            throws ExecutionException, InterruptedException {
        
        PostResponseDTO responseDTO = new PostResponseDTO();
        
        // Set sender and recipient
        responseDTO.setSenderId(rejectionRequest.getSenderId());
        responseDTO.setSenderName(rejectionRequest.getSenderName());
        responseDTO.setSenderType(rejectionRequest.getSenderType());
        responseDTO.setRecipientIds(rejectionRequest.getRecipientIds());
        responseDTO.setRecipientNames(rejectionRequest.getRecipientNames());
        responseDTO.setRecipientType(rejectionRequest.getRecipientType());
        
        // Set entity references
        responseDTO.setOpportunityId(rejectionRequest.getOpportunityId());
        responseDTO.setOpportunityName(rejectionRequest.getOpportunityName());
        responseDTO.setOrganizationId(rejectionRequest.getOrganizationId());
        responseDTO.setOrganizationName(rejectionRequest.getOrganizationName());
        responseDTO.setAdminId(rejectionRequest.getAdminId());
        
        // Set rejection status and reason
        responseDTO.setStatus("rejected");
        responseDTO.setResponseMessage(rejectionRequest.getReason());

        return createPostResponse(responseDTO);
    }

    private void setBaseFields(Notification notification, BaseNotificationDTO dto) {
        notification.setSenderId(dto.getSenderId());
        notification.setSenderName(dto.getSenderName());
        notification.setSenderType(dto.getSenderType());
        notification.setRecipientIds(dto.getRecipientIds());
        notification.setRecipientNames(dto.getRecipientNames());
        notification.setRecipientType(dto.getRecipientType());
        notification.setTimestamp(com.google.cloud.Timestamp.now());
        notification.setRead(false);
    }

    private void setApplicationRequestFields(Notification notification, ApplicationRequestDTO dto) {
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        notification.setVolunteerId(dto.getVolunteerId());
        notification.setVolunteerName(dto.getVolunteerName());
        notification.setTeamId(dto.getTeamId());
        notification.setTeamName(dto.getTeamName());
        notification.setLeaderIds(dto.getLeaderIds());
    }

    private void setApplicationResponseFields(Notification notification, ApplicationResponseDTO dto) {
        notification.setOpportunityId(dto.getOpportunityId());
        notification.setOpportunityName(dto.getOpportunityName());
        notification.setOrganizationId(dto.getOrganizationId());
        notification.setOrganizationName(dto.getOrganizationName());
        notification.setVolunteerId(dto.getVolunteerId());
        notification.setVolunteerName(dto.getVolunteerName());
        notification.setTeamId(dto.getTeamId());
        notification.setTeamName(dto.getTeamName());
        notification.setLeaderIds(dto.getLeaderIds());
        notification.setResponseMessage(dto.getResponseMessage());
    }

    public List<Notification> getNotificationsWithFilters(String recipientId, String type, 
            Boolean unreadOnly, String priority) throws ExecutionException, InterruptedException {
        
        return notificationRepository.findWithFilters(recipientId, type, unreadOnly, priority);
    }

    public void markAllAsRead(String recipientId) throws ExecutionException, InterruptedException {
        notificationRepository.markAllAsRead(recipientId);
    }

    // Helper method to determine notification type for frontend
    public String determineFrontendType(Notification notification) {
        switch(notification.getType()) {
            case "application_request":
            case "application_response":
            case "post_request":
            case "post_response":
                return "organization";
            case "team_registration_request":
            case "team_registration_response":
                return "team";
            default:
                return "system";
        }
    }

    // Helper method to determine priority
    public String determinePriority(Notification notification) {
        // High priority for unread requests and urgent responses
        if (!notification.isRead() && 
            (notification.getType().contains("request") || 
             notification.getStatus() != null && notification.getStatus().equals("rejected"))) {
            return "high";
        }
        return "normal";
    }

    // GET methods
    public List<Notification> getNotificationsByRecipient(String recipientId) throws ExecutionException, InterruptedException {
        return notificationRepository.findByRecipientId(recipientId);
    }

    public List<Notification> getUnreadNotificationsByRecipient(String recipientId) throws ExecutionException, InterruptedException {
        return notificationRepository.findByRecipientIdAndRead(recipientId, false);
    }

    public Notification getNotificationById(String notificationId) throws ExecutionException, InterruptedException {
        return notificationRepository.findById(notificationId);
    }

    public void markNotificationAsRead(String notificationId) throws ExecutionException, InterruptedException {
        notificationRepository.markAsRead(notificationId);
    }

    // OPTIONAL: UI formatting helper (remove if not needed)
    public Map<String, Object> formatNotificationForUI(Notification notification) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", notification.getId());
        formatted.put("title", notification.getTitle());
        formatted.put("message", notification.getContent());
        formatted.put("time", formatTimeForUI(notification.getTimestamp()));
        formatted.put("notificationType", determineFrontendType(notification));
        formatted.put("priority", determinePriority(notification));
        formatted.put("read", notification.isRead());
        formatted.put("icon", getNotificationIconName(notification.getType()));
        formatted.put("metadata", notification);
        
        return formatted;
    }

    private String formatTimeForUI(Timestamp timestamp) {
        if (timestamp == null) return "Just now";
        
        try {
            Date date = timestamp.toDate();
            long diffInMinutes = (System.currentTimeMillis() - date.getTime()) / (1000 * 60);
            
            if (diffInMinutes < 1) return "Just now";
            if (diffInMinutes < 60) return diffInMinutes + " minute" + (diffInMinutes != 1 ? "s" : "") + " ago";
            
            long diffInHours = diffInMinutes / 60;
            if (diffInHours < 24) return diffInHours + " hour" + (diffInHours != 1 ? "s" : "") + " ago";
            
            long diffInDays = diffInHours / 24;
            return diffInDays + " day" + (diffInDays != 1 ? "s" : "") + " ago";
        } catch (Exception e) {
            return "Recently";
        }
    }

    private String getNotificationIconName(String type) {
        switch(type) {
            case "team_registration_request": return "User";
            case "team_registration_response": return "UserCheck";
            case "application_request":
            case "application_response": return "Users";
            case "post_request":
            case "post_response": return "Info";
            default: return "Bell";
        }
    }
}