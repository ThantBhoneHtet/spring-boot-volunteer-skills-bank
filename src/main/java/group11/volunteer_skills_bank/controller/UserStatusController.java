package group11.volunteer_skills_bank.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.service.OrganizationService;
import group11.volunteer_skills_bank.service.VolunteerService;

@RestController
@CrossOrigin("*")
@RequestMapping("/users")
public class UserStatusController {

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RestTemplate restTemplate;


    @PostMapping("/{userId}/status/{isOnline}")
    public void updateUserStatus(@PathVariable String userId, @PathVariable boolean isOnline){
        boolean updated = false;

        Volunteer volunteer = volunteerService.findVolunteerById(userId).block();
        if (volunteer != null) {
            volunteer.setIsOnline(isOnline);
            volunteerService.saveVolunteer(volunteer);
            updated = true;
        }
        else {
            Organization organization = organizationService.findOrganization(userId).block();
            organization.setIsOnline(isOnline);
            organizationService.saveOrganization(organization);
            updated = true;
        }

        if (updated) {
            // Broadcast status change over WebSocket
            messagingTemplate.convertAndSend(
                "/topic/user-status", 
                Map.of("userId", userId, "isOnline", isOnline)
            );
        }
    }

    @PostMapping("/sentiment")
    public ResponseEntity<String> analyzeSentiment(@RequestParam String text) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Text parameter is required");
        }
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", "2Uta2Bt7tmyU5fPlYxD4ytQDKrUqxXZr");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);
        
        // Create the HTTP entity
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.apilayer.com/sentiment/analysis",
                HttpMethod.POST,
                entity,
                String.class
            );
            
            return response;
        } catch (Exception e) {
            // Handle exceptions appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error calling sentiment analysis API: " + e.getMessage());
        }
    }
}
