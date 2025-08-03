package group11.volunteer_skills_bank.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.dto.organization.OrganizationRequestDto;
import group11.volunteer_skills_bank.model.OrganizationRequest;
import group11.volunteer_skills_bank.service.OrganizationRequestService;
import group11.volunteer_skills_bank.service.OrganizationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/organizationRequest")
@CrossOrigin("*")
public class OrganizationRequestController 
{
    @Autowired
    private OrganizationRequestService organizationRequestService;

    @Autowired
    private OrganizationService organizationService;

    @PostMapping()
    public ResponseEntity<?> createOrganizationRequest(@RequestBody OrganizationRequestDto organizationRequestDto) {
        return organizationRequestService.createOrganizationRequest(organizationRequestDto);
    }

    @GetMapping()
    public Flux<OrganizationRequest> getAllOrganizationRequests() 
    {
        return organizationRequestService.getAllOrganizationRequests();
    }

    @PostMapping("/reject")
    public Mono<OrganizationRequest> rejectOrganizationRequest(@RequestBody Map<String, String> organizationRequestDto) 
    {
        return organizationRequestService.rejectOrganizationRequest(organizationRequestDto.get("organizationId"),
                organizationRequestDto.get("rejectionReason"));
    }
    
    @PostMapping("/approve")
    public Mono<ResponseEntity<String>> approveRequest(@RequestBody Map<String, String> request) {
        String organizationId = request.get("organizationId");
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Organization ID is required"));
        }

        // Subscribe to the service's Mono and transform the result
        return organizationService.approveRequest(organizationId)
                .map(savedOrganization -> ResponseEntity.ok(
                        "Organization request approved successfully for: " + savedOrganization.getOrganizationName()))
                .onErrorResume(e -> {
                    // Handle specific errors from the service layer gracefully
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()));
                    }
                    if (e instanceof IllegalStateException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()));
                    }
                    // Fallback for other unexpected errors
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("An internal error occurred."));
                });
    }

    
}
