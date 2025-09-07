package group11.volunteer_skills_bank.controller;

import java.util.List;

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

import group11.volunteer_skills_bank.dto.opportunity.OpportunityApprovalRequest;
import group11.volunteer_skills_bank.dto.opportunity.OpportunityCreateRequest;
import group11.volunteer_skills_bank.dto.opportunity.OpportunityStatusUpdateRequest;
import group11.volunteer_skills_bank.model.Opportunity;
import group11.volunteer_skills_bank.service.OpportunityService;
import group11.volunteer_skills_bank.service.OpportunityStatusService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/opportunities")
@CrossOrigin("*")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final OpportunityStatusService opportunityStatusService;

    public OpportunityController(OpportunityService opportunityService,
            OpportunityStatusService opportunityStatusService) {
        this.opportunityService = opportunityService;
        this.opportunityStatusService = opportunityStatusService;
    }

    @PostMapping
    public ResponseEntity<Mono<Opportunity>> create(@Valid @RequestBody OpportunityCreateRequest request) {
        Opportunity opportunity = new Opportunity();
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setOrganizationId(request.getOrganizationId());
        opportunity.setSkillsRequired(request.getSkillsRequired());

        // Handle location with city
        if (request.getLocation() != null) {
            Opportunity.Location location = new Opportunity.Location();
            location.setAddress(request.getLocation().getAddress());
            location.setCity(request.getLocation().getCity());
            opportunity.setLocation(location);
        }

        // Handle volunteer work dates
        if (request.getStartDateMillis() != null) {
            opportunity.setStartDate(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    request.getStartDateMillis() / 1000, (int) ((request.getStartDateMillis() % 1000) * 1_000_000)));
        }
        if (request.getEndDateMillis() != null) {
            opportunity.setEndDate(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    request.getEndDateMillis() / 1000, (int) ((request.getEndDateMillis() % 1000) * 1_000_000)));
        }

        // Handle application deadline
        if (request.getDeadlineMillis() != null) {
            opportunity.setDeadline(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    request.getDeadlineMillis() / 1000, (int) ((request.getDeadlineMillis() % 1000) * 1_000_000)));
        }

        opportunity.setTimeCommitment(request.getTimeCommitment());
        opportunity.setVolunteersNeeded(request.getVolunteersNeeded());
        opportunity.setCategories(request.getCategories());
        opportunity.setType(request.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(opportunity));
    }

    @GetMapping
    public ResponseEntity<Flux<Opportunity>> list() {
        return ResponseEntity.ok(opportunityService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<Opportunity>> getById(@PathVariable String id) {
        return ResponseEntity.ok(opportunityService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mono<Opportunity>> update(@PathVariable String id,
            @RequestBody OpportunityCreateRequest request) {
        // Security check: Only organization owners can edit their own posts
        // This will be enforced at the service level
        Opportunity update = new Opportunity();
        update.setTitle(request.getTitle());
        update.setDescription(request.getDescription());
        update.setSkillsRequired(request.getSkillsRequired());
        update.setCategories(request.getCategories());
        update.setTimeCommitment(request.getTimeCommitment());
        update.setVolunteersNeeded(request.getVolunteersNeeded());
        update.setType(request.getType());

        // Handle location with city
        if (request.getLocation() != null) {
            Opportunity.Location location = new Opportunity.Location();
            location.setAddress(request.getLocation().getAddress());
            location.setCity(request.getLocation().getCity());
            update.setLocation(location);
        }

        // Handle volunteer work dates
        if (request.getStartDateMillis() != null) {
            update.setStartDate(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    request.getStartDateMillis() / 1000, (int) ((request.getStartDateMillis() % 1000) * 1_000_000)));
        }
        if (request.getEndDateMillis() != null) {
            update.setEndDate(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    request.getEndDateMillis() / 1000, (int) ((request.getEndDateMillis() % 1000) * 1_000_000)));
        }

        // Handle application deadline
        if (request.getDeadlineMillis() != null) {
            update.setDeadline(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                    request.getDeadlineMillis() / 1000, (int) ((request.getDeadlineMillis() % 1000) * 1_000_000)));
        }

        return ResponseEntity.ok(opportunityService.update(id, update));
    }

    // New endpoint for updating opportunity status (e.g., cancelling posts)
    @PutMapping("/{id}/status")
    public ResponseEntity<Mono<Opportunity>> updateStatus(@PathVariable String id,
            @RequestBody OpportunityStatusUpdateRequest request) {
        return ResponseEntity.ok(opportunityService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Mono<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(opportunityService.delete(id));
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<Flux<Opportunity>> listByOrganization(@PathVariable("orgId") String organizationId) {
        return ResponseEntity.ok(opportunityService.listByOrganization(organizationId));
    }

    @GetMapping("/recommended/{volunteerId}")
    public ResponseEntity<Flux<Opportunity>> getRecommended(@PathVariable String volunteerId) {
        return ResponseEntity.ok(opportunityService.getRecommended(volunteerId));
    }

    // Admin endpoints for post management
    @PostMapping("/approve")
    public ResponseEntity<Mono<Opportunity>> approveOpportunity(
            @Valid @RequestBody OpportunityApprovalRequest request) {
        return ResponseEntity.ok(opportunityService.approveOpportunity(
                request.getOpportunityId(),
                request.getApprovalStatus(),
                request.getAdminNotes()));
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<Flux<Opportunity>> getPendingApproval() {
        return ResponseEntity.ok(opportunityService.getPendingApproval());
    }

    @GetMapping("/admin/approved")
    public ResponseEntity<Flux<Opportunity>> getApprovedOpportunities() {
        return ResponseEntity.ok(opportunityService.getApprovedOpportunities());
    }

    @GetMapping("/admin/rejected")
    public ResponseEntity<Flux<Opportunity>> getRejectedOpportunities() {
        return ResponseEntity.ok(opportunityService.getRejectedOpportunities());
    }

    // New endpoint to get all opportunities for PostManagement
    @GetMapping("/admin/all")
    public ResponseEntity<Flux<Opportunity>> getAllOpportunities() {
        return ResponseEntity.ok(opportunityService.listAll());
    }

    // Test endpoint to create a sample opportunity for testing
    @PostMapping("/test/create-sample")
    public ResponseEntity<Mono<Opportunity>> createSampleOpportunity(@RequestBody OpportunityCreateRequest request) {
        Opportunity sample = new Opportunity();
        sample.setTitle(request.getTitle() != null ? request.getTitle() : "Sample Volunteer Opportunity");
        sample.setDescription(request.getDescription() != null ? request.getDescription()
                : "This is a sample opportunity for testing.");
        sample.setOrganizationId(request.getOrganizationId());
        sample.setSkillsRequired(request.getSkillsRequired() != null ? request.getSkillsRequired()
                : List.of("Communication", "Leadership"));
        sample.setCategories(
                request.getCategories() != null ? request.getCategories() : List.of("Education", "Community"));
        sample.setTimeCommitment(
                request.getTimeCommitment() != null ? request.getTimeCommitment() : "2-3 hours per week");
        sample.setVolunteersNeeded(request.getVolunteersNeeded() != null ? request.getVolunteersNeeded() : 5);
        sample.setType(request.getType() != null ? request.getType() : "individual");

        // Set sample dates if not provided
        if (request.getStartDateMillis() == null) {
            long oneWeekFromNow = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
            sample.setStartDate(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(oneWeekFromNow / 1000, 0));
        }
        if (request.getEndDateMillis() == null) {
            long threeWeeksFromNow = System.currentTimeMillis() + (21L * 24 * 60 * 60 * 1000);
            sample.setEndDate(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(threeWeeksFromNow / 1000, 0));
        }
        if (request.getDeadlineMillis() == null) {
            long fiveDaysFromNow = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000);
            sample.setDeadline(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(fiveDaysFromNow / 1000, 0));
        }

        // Set sample location
        Opportunity.Location location = new Opportunity.Location();
        location.setAddress("Sample Address, Sample City");
        location.setCity("Sample City");
        sample.setLocation(location);

        sample.setStatus("active");
        sample.setApprovalStatus("pending");

        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(sample));
    }

    @PostMapping("/admin/update-statuses")
    public ResponseEntity<String> manuallyUpdateStatuses() {
        opportunityStatusService.updateExpiredOpportunities();
        return ResponseEntity.ok("Status update process initiated");
    }

    @GetMapping("/admin/status-config")
    public ResponseEntity<String> getStatusConfiguration() {
        String config = opportunityStatusService.getConfigurationStatus();
        return ResponseEntity.ok(config);
    }

    // Endpoint for frontend to get active opportunities
    @GetMapping("/active")
    public ResponseEntity<Flux<Opportunity>> getActiveOpportunities() {
        return ResponseEntity.ok(opportunityService.getActiveOpportunities());
    }

    // Endpoint to get all unique skills from opportunities
    @GetMapping("/skills")
    public Mono<ResponseEntity<List<String>>> getAllSkills() {
        return opportunityService.getAllUniqueSkills()
                .collectList() // Convert Flux<String> -> Mono<List<String>>
                .map(list -> ResponseEntity.ok(list));
    }

    // Check if opportunity is available for applications (volunteersNeeded > 0)
    @GetMapping("/{id}/availability")
    public ResponseEntity<Mono<Object>> checkAvailability(@PathVariable String id) {
        return ResponseEntity.ok(opportunityService.checkAvailability(id));
    }

    @GetMapping("/count/approved")
    public Mono<Long> getTotalApprovedOpportunities() {
        return opportunityService.getTotalApprovedOpportunities();
    }
}
