package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.model.Opportunity;
import group11.volunteer_skills_bank.service.OpportunityCompleteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/opportunities")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OpportunityCompleteController {

    private static final Logger logger = LoggerFactory.getLogger(OpportunityCompleteController.class);
    private final OpportunityCompleteService opportunityCompleteService;

    @GetMapping("/completed/volunteer/{volunteerId}")
    public ResponseEntity<?> getCompletedForVolunteer(@PathVariable String volunteerId) {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getCompletedOpportunitiesForVolunteer(volunteerId);
            if (opportunities.isEmpty()) {
                logger.info("No completed opportunities found for volunteerId: {}", volunteerId);
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(opportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching completed opportunities for volunteerId {}: {}", volunteerId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch completed opportunities: " + e.getMessage());
        }
    }

    @GetMapping("/completed/team/{teamId}")
    public ResponseEntity<?> getCompletedForTeam(@PathVariable String teamId) {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getCompletedOpportunitiesForTeam(teamId);
            if (opportunities.isEmpty()) {
                logger.info("No completed opportunities found for teamId: {}", teamId);
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(opportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching completed opportunities for teamId {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch completed opportunities: " + e.getMessage());
        }
    }
    
    @GetMapping("/scheduled/volunteer/{volunteerId}")
    public ResponseEntity<?> getScheduledForVolunteer(@PathVariable String volunteerId) {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getScheduledOpportunitiesForVolunteer(volunteerId);
            if (opportunities.isEmpty()) {
                logger.info("No scheduled opportunities found for volunteerId: {}", volunteerId);
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(opportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching scheduled opportunities for volunteerId {}: {}", volunteerId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch scheduled opportunities: " + e.getMessage());
        }
    }
    
    @GetMapping("/scheduled/team/{teamId}")
    public ResponseEntity<?> getScheduledForTeam(@PathVariable String teamId) {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getScheduledOpportunitiesForTeam(teamId);
            if (opportunities.isEmpty()) {
                logger.info("No scheduled opportunities found for teamId: {}", teamId);
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(opportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching scheduled opportunities for teamId {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch scheduled opportunities: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllOpportunities() {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getAllOpportunities();
            if (opportunities.isEmpty()) {
                logger.info("No opportunities found");
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(opportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching all opportunities: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch opportunities: " + e.getMessage());
        }
    }
    
    @GetMapping("/completed/all")
    public ResponseEntity<?> getAllCompletedOpportunities() {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getAllOpportunities();
            List<Opportunity> completedOpportunities = opportunities.stream()
                    .filter(opportunity -> "complete".equalsIgnoreCase(opportunity.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

            if (completedOpportunities.isEmpty()) {
                logger.info("No completed opportunities found");
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(completedOpportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching all completed opportunities: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch completed opportunities: " + e.getMessage());
        }
    }
    
    @GetMapping("/active/team/{teamId}")
    public ResponseEntity<?> getActiveForTeam(@PathVariable String teamId) {
        try {
            List<Opportunity> opportunities = opportunityCompleteService.getActiveOpportunitiesForTeam(teamId);
            if (opportunities.isEmpty()) {
                logger.info("No active opportunities found for teamId: {}", teamId);
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(opportunities);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching active opportunities for teamId {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch active opportunities: " + e.getMessage());
        }
    }
}