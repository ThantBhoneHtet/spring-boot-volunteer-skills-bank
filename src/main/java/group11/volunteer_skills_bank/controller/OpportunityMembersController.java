package group11.volunteer_skills_bank.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.model.OpportunityMembers;
import group11.volunteer_skills_bank.service.OpportunityMembersService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/opportunity-members")
@CrossOrigin(origins = "*")
public class OpportunityMembersController {

    private final OpportunityMembersService opportunityMembersService;

    @Autowired
    public OpportunityMembersController(OpportunityMembersService opportunityMembersService) {
        this.opportunityMembersService = opportunityMembersService;
    }

    /**
     * Retrieves opportunity members by opportunity ID.
     *
     * @param opportunityId The ID of the opportunity.
     * @return A Mono with a ResponseEntity containing the opportunity members.
     */
    @GetMapping("/opportunity/{opportunityId}")
    public Mono<ResponseEntity<OpportunityMembers>> getByOpportunityId(@PathVariable String opportunityId) {
        System.out.println("OpportunityMembersController: Fetching members for opportunity ID: " + opportunityId);
        if (opportunityId == null || opportunityId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
        }

        return opportunityMembersService.getByOpportunityId(opportunityId)
                .map(members -> ResponseEntity.ok(members))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("OpportunityMembersController: Error fetching members for opportunity ID " + opportunityId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }

    /**
     * Retrieves all opportunity members by organization ID.
     *
     * @param organizationId The ID of the organization.
     * @return A Mono with a ResponseEntity containing the list of opportunity members.
     */
    @GetMapping("/organization/{organizationId}")
    public Mono<ResponseEntity<List<OpportunityMembers>>> getByOrganizationId(@PathVariable String organizationId) {
        System.out.println("OpportunityMembersController: Fetching members for organization ID: " + organizationId);
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of()));
        }

        return opportunityMembersService.getByOrganizationId(organizationId)
                .collectList()
                .map(membersList -> ResponseEntity.ok(membersList))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("OpportunityMembersController: Error fetching members for organization ID " + organizationId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of()));
                });
    }

    /**
     * Retrieves volunteer count for a specific opportunity.
     *
     * @param opportunityId The ID of the opportunity.
     * @return A Mono with a ResponseEntity containing the volunteer count.
     */
   @GetMapping("/opportunity/{opportunityId}/volunteer-count")
public Mono<ResponseEntity<Map<String, Object>>> getVolunteerCount(@PathVariable String opportunityId) {
    System.out.println("OpportunityMembersController: Fetching volunteer count for opportunity ID: " + opportunityId);
    if (opportunityId == null || opportunityId.trim().isEmpty()) {
        Map<String, Object> errorResponse = new java.util.HashMap<>();
        errorResponse.put("error", "Opportunity ID cannot be null or empty");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    return opportunityMembersService.getByOpportunityId(opportunityId)
            .map(members -> {
                int volunteerCount = members.getVolunteers() != null ? members.getVolunteers().size() : 0;
                int teamCount = members.getTeams() != null ? members.getTeams().size() : 0;
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("opportunityId", opportunityId);
                response.put("volunteerCount", volunteerCount);
                response.put("teamCount", teamCount);
                response.put("totalMembers", volunteerCount + teamCount);
                return ResponseEntity.ok(response);
            })
            .switchIfEmpty(Mono.fromSupplier(() -> {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("opportunityId", opportunityId);
                response.put("volunteerCount", 0);
                response.put("teamCount", 0);
                response.put("totalMembers", 0);
                return ResponseEntity.ok(response);
            }))
            .onErrorResume(Exception.class, e -> {
                System.err.println("OpportunityMembersController: Error fetching volunteer count for opportunity ID " + opportunityId + ": " + e.getMessage());
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "Failed to fetch volunteer count: " + e.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
            });
}
}
