package group11.volunteer_skills_bank.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.dto.application.ApplicationApprovalRequest;
import group11.volunteer_skills_bank.dto.application.ApplicationCreateRequest;
import group11.volunteer_skills_bank.model.Application;
import group11.volunteer_skills_bank.service.ApplicationService;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Handles the creation of a new application with auto-generated ID.
     *
     * @param request The application creation request.
     * @return A Mono with a ResponseEntity containing the generated application ID or an error message.
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> create(@Valid @RequestBody ApplicationCreateRequest request) {
        System.out.println("ApplicationController: Received request to create application");
        if (request == null) {
            System.err.println("ApplicationController: Request is null");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Request cannot be null")));
        }

        return applicationService.createWithOpportunityValidation(request)
                .map(application -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("applicationId", application.getApplicationId())))
                .onErrorResume(IllegalStateException.class, e -> {
                    System.err.println("ApplicationController: Duplicate application detected: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", e.getMessage())));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    System.err.println("ApplicationController: Invalid input: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", e.getMessage())));
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error creating application: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to create application: " + e.getMessage())));
                });
    }

    /**
     * Retrieves an application by its auto-generated ID.
     *
     * @param id The auto-generated application ID.
     * @return A Mono with a ResponseEntity containing the application ID and details or an error message.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> getById(@PathVariable String id) {
        System.out.println("ApplicationController: Fetching application for ID: " + id);
        if (id == null || id.trim().isEmpty()) {
            System.err.println("ApplicationController: Invalid application ID");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Application ID cannot be null or empty")));
        }

        return applicationService.getById(id)
                .map(application -> ResponseEntity.ok(Map.of(
                        "applicationId", application.getApplicationId(),
                        "opportunityId", application.getOpportunityId(),
                        "organizationId", application.getOrganizationId(),
                        "type", application.getType(),
                        "status", application.getStatus(),
                        "email", application.getEmail() != null ? application.getEmail() : "",
                        "leaderEmail", application.getLeaderEmail() != null ? application.getLeaderEmail() : ""
                )))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Application not found for ID: " + id))))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching application for ID " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to fetch application: " + e.getMessage())));
                });
    }

    /**
     * Retrieves applications by organization ID.
     *
     * @param organizationId The ID of the organization.
     * @return A Mono with a ResponseEntity containing the list of applications.
     */
    @GetMapping("/organization/{orgId}")
    public Mono<ResponseEntity<List<Application>>> getByOrganization(@PathVariable("orgId") String organizationId) {
        System.out.println("ApplicationController: Fetching applications for organization ID: " + organizationId);
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getByOrganizationId(organizationId)
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching applications for organization ID " + organizationId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Retrieves applications by opportunity ID.
     *
     * @param opportunityId The ID of the opportunity.
     * @return A Mono with a ResponseEntity containing the list of applications.
     */
    @GetMapping("/opportunity/{opportunityId}")
    public Mono<ResponseEntity<List<Application>>> getByOpportunity(@PathVariable String opportunityId) {
        System.out.println("ApplicationController: Fetching applications for opportunity ID: " + opportunityId);
        if (opportunityId == null || opportunityId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getByOpportunityId(opportunityId)
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching applications for opportunity ID " + opportunityId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Approves an application, restricted to the organization.
     *
     * @param id The auto-generated application ID.
     * @param request The approval request.
     * @return A Mono with a ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}/approve")
    public Mono<ResponseEntity<Map<String, String>>> approveApplication(
            @PathVariable String id,
            @Valid @RequestBody ApplicationApprovalRequest request) {
        System.out.println("ApplicationController: Approving application " + id);
        System.out.println("Approve request: oppId=" + request.getOpportunityId() + ", orgId=" + request.getOrganizationId());

        if (id == null || id.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Application ID cannot be null or empty")));
        }
        if (request.getOrganizationId() == null || request.getOpportunityId() == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Organization ID and Opportunity ID cannot be null")));
        }

        return applicationService.approveApplication(id, request.getOpportunityId(), request.getOrganizationId())
                .map(application -> ResponseEntity.ok(Map.of("message", "Application approved successfully", "applicationId", id)))
                .onErrorResume(IllegalStateException.class, e -> {
                    System.err.println("ApplicationController: Forbidden approval for ID " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", e.getMessage())));
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error approving application ID " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to approve application: " + e.getMessage())));
                });
    }

    /**
     * Rejects an application, restricted to the organization.
     *
     * @param id The auto-generated application ID.
     * @param request The rejection request.
     * @return A Mono with a ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}/reject")
    public Mono<ResponseEntity<Map<String, String>>> rejectApplication(
            @PathVariable String id,
            @Valid @RequestBody ApplicationApprovalRequest request) {
        System.out.println("ApplicationController: Rejecting application " + id);
        if (id == null || id.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Application ID cannot be null or empty")));
        }
        if (request.getOrganizationId() == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Organization ID cannot be null")));
        }

        String reason = request.getReason() != null ? request.getReason() : "";
        return applicationService.rejectApplication(id, reason, request.getOrganizationId())
                .map(application -> ResponseEntity.ok(Map.of("message", "Application rejected successfully", "applicationId", id)))
                .onErrorResume(IllegalStateException.class, e -> {
                    System.err.println("ApplicationController: Forbidden rejection for ID " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", e.getMessage())));
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error rejecting application ID " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to reject application: " + e.getMessage())));
                });
    }

    /**
     * Deletes an application by its ID.
     *
     * @param id The auto-generated application ID.
     * @return A Mono with a ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> delete(@PathVariable String id) {
        System.out.println("ApplicationController: Deleting application " + id);
        if (id == null || id.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Application ID cannot be null or empty")));
        }

        return applicationService.delete(id)
                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Application deleted successfully"))))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error deleting application ID " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to delete application: " + e.getMessage())));
                });
    }

    /**
     * Retrieves pending applications for an organization.
     *
     * @param organizationId The ID of the organization.
     * @return A Mono with a ResponseEntity containing the list of pending applications.
     */
    @GetMapping("/organization/{orgId}/pending")
    public Mono<ResponseEntity<List<Application>>> getPendingApplicationsForOrganization(@PathVariable("orgId") String organizationId) {
        System.out.println("ApplicationController: Fetching pending applications for organization " + organizationId);
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getByOrganizationId(organizationId)
                .filter(app -> "pending".equalsIgnoreCase(app.getStatus()))
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching pending applications for organization ID " + organizationId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Retrieves approved applications for an organization.
     *
     * @param organizationId The ID of the organization.
     * @return A Mono with a ResponseEntity containing the list of approved applications.
     */
    @GetMapping("/organization/{orgId}/approved")
    public Mono<ResponseEntity<List<Application>>> getApprovedApplicationsForOrganization(@PathVariable("orgId") String organizationId) {
        System.out.println("ApplicationController: Fetching approved applications for organization " + organizationId);
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getByOrganizationId(organizationId)
                .filter(app -> "approved".equalsIgnoreCase(app.getStatus()))
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching approved applications for organization ID " + organizationId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Retrieves rejected applications for an organization.
     *
     * @param organizationId The ID of the organization.
     * @return A Mono with a ResponseEntity containing the list of rejected applications.
     */
    @GetMapping("/organization/{orgId}/rejected")
    public Mono<ResponseEntity<List<Application>>> getRejectedApplicationsForOrganization(@PathVariable("orgId") String organizationId) {
        System.out.println("ApplicationController: Fetching rejected applications for organization " + organizationId);
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getByOrganizationId(organizationId)
                .filter(app -> "rejected".equalsIgnoreCase(app.getStatus()))
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching rejected applications for organization ID " + organizationId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Retrieves applications with volunteer details for a specific opportunity.
     * This endpoint enriches application data with volunteer information.
     *
     * @param opportunityId The ID of the opportunity.
     * @return A Mono with a ResponseEntity containing the list of applications with volunteer details.
     */
    @GetMapping("/opportunity/{opportunityId}/enriched")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getEnrichedApplications(@PathVariable String opportunityId) {
        System.out.println("ApplicationController: Fetching enriched applications for opportunity ID: " + opportunityId);
        if (opportunityId == null || opportunityId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getApplicationsWithVolunteerDetails(opportunityId)
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching enriched applications for opportunity ID " + opportunityId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }

    /**
     * Retrieves applications with volunteer details for a specific opportunity.
     * This endpoint enriches application data with volunteer information.
     *
     * @param opportunityId The ID of the opportunity.
     * @return A Mono with a ResponseEntity containing the list of applications with volunteer details.
     */
    @GetMapping("/opportunity/{opportunityId}/with-volunteer-details")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getApplicationsWithVolunteerDetails(@PathVariable String opportunityId) {
        System.out.println("ApplicationController: Fetching applications with volunteer details for opportunity ID: " + opportunityId);
        if (opportunityId == null || opportunityId.trim().isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return applicationService.getApplicationsWithVolunteerDetails(opportunityId)
                .collectList()
                .map(applications -> ResponseEntity.ok(applications))
                .onErrorResume(Exception.class, e -> {
                    System.err.println("ApplicationController: Error fetching applications with volunteer details for opportunity ID " + opportunityId + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(List.of()));
                });
    }
}