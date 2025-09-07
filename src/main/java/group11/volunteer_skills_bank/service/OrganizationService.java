package group11.volunteer_skills_bank.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import group11.volunteer_skills_bank.dto.organization.OrganizationUpdate;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.repository.OrganizationRepo;
import group11.volunteer_skills_bank.repository.OpportunityRepository;
import group11.volunteer_skills_bank.repository.ApplicationRepository;
import group11.volunteer_skills_bank.utils.ActivityLogger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrganizationService {
    private OrganizationRepo organizationRepo;
    private OrganizationRequestService organizationRequestService;
    private PasswordEncoder passwordEncoder;
    private S3Service s3Service;
    private OpportunityStatusService opportunityStatusService;
    private OpportunityRepository opportunityRepository;
    private ApplicationRepository applicationRepository;
    @Autowired
    private Firestore firestore;
    @Autowired
    private ActivityLogger activityLogger;

    public OrganizationService(OrganizationRepo organizationRepo, OrganizationRequestService organizationRequestService,
            PasswordEncoder passwordEncoder, S3Service s3Service, OpportunityStatusService opportunityStatusService,
            OpportunityRepository opportunityRepository, ApplicationRepository applicationRepository) {
        this.organizationRepo = organizationRepo;
        this.organizationRequestService = organizationRequestService;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
        this.opportunityStatusService = opportunityStatusService;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
    }

    public Flux<Organization> listAll() {
        return organizationRepo.findAll();
    }

    public Mono<Organization> approveRequest(String organizationId) {
        return organizationRequestService.findOrganization(organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization request not found")))
                .flatMap(request -> {
                    // Check if the request is in a pending state
                    if (!"pending".equals(request.getStatus())) {
                        return Mono.error(new IllegalStateException("Request is not in a pending state"));
                    }

                    // 1. Update the status of the request and save it first.
                    request.setStatus("approved");
                    return organizationRequestService.saveOrganizationRequest(request)
                            .flatMap(updatedRequest -> {
                                // 2. Create and save the new Organization from the updated request data
                                Organization organization = new Organization();
                                organization.setOrganizationId(updatedRequest.getOrganizationId());
                                organization.setOrganizationName(updatedRequest.getOrganizationName());
                                organization.setOrganizationType(updatedRequest.getOrganizationType());
                                organization.setEmail(updatedRequest.getEmail());
                                organization.setPassword(updatedRequest.getPassword()); // Hashed password is moved here
                                organization.setAddress(updatedRequest.getAddress());
                                organization.setDescription(updatedRequest.getDescription());
                                organization.setCreatedAt(updatedRequest.getCreatedAt());
                                organization.setStatus("active");

                                return organizationRepo.save(organization)
                                        .doOnSuccess(savedOrganization -> {
                                            // Log organization approval activity
                                            activityLogger.logOrganizationApproval(
                                                    organizationId,
                                                    request.getOrganizationName(),
                                                    "system", // You might want to pass actual admin ID
                                                    "System Admin");
                                        });
                            });
                });
    }

    public Organization login(String email, String password) {
        Organization organization = organizationRepo.findByEmail(email).block();
        if (organization == null || !passwordEncoder.matches(password, organization.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        organization.setLastLogin(Timestamp.now());
        Organization savedOrganization = organizationRepo.save(organization).block();
        return savedOrganization;
    }

    public Mono<Organization> findOrganizationByEmail(String email) {
        return organizationRepo.findByEmail(email);
    }

    public Mono<Organization> findOrganization(String organizationId) {
        return organizationRepo.findById(organizationId);
    }

    public void logout(String id) {
        Organization organization = organizationRepo.findById(id).block();
        if (organization != null) {
            organization.setLastLogin(null);
            organizationRepo.save(organization).block();
        }
    }

    public Mono<Organization> updateOrganization(String id, OrganizationUpdate dto, MultipartFile image)
            throws IOException, ExecutionException, InterruptedException {
        Organization organization = organizationRepo.findById(id).block();
        if (organization == null) {
            throw new RuntimeException("Volunteer with ID " + id + " not found.");
        }
        if (image != null) {
            // Optional: Consider deleting the old image from S3 if the URL is changing.
            // This would require fetching the old avatarUrl before setting the new one.
            String newImageUrl = s3Service.uploadFile(image);
            organization.setLogoUrl(newImageUrl); // Set the new URL
        }

        if (dto.location() != null) {
            String city = dto.location().city() != null ? dto.location().city()
                    : (organization.getLocation() != null ? organization.getLocation().getCity() : null);
            String country = dto.location().country() != null ? dto.location().country()
                    : (organization.getLocation() != null ? organization.getLocation().getCountry() : null);
            organization.setLocation(new Organization.Location(city, country));
        }
        if (dto.discription() != null) {
            organization.setDescription(dto.discription());
        }
        if (dto.organizationName() != null) {
            organization.setOrganizationName(dto.organizationName());
        }
        if (dto.organizationType() != null) {
            organization.setOrganizationType(dto.organizationType());
        }
        if (dto.website() != null) {
            organization.setWebsite(dto.website());
        }
        if (dto.phoneNumber() != null) {
            organization.setPhoneNumber(dto.phoneNumber());
        }
        ApiFuture<WriteResult> future = firestore.collection("organizations").document(id).set(organization);
        future.get(); // Wait for the write to complete
        return organizationRepo.save(organization);
    }

    public void saveOrganization(Organization organization) {
        organizationRepo.save(organization).block();
    }

    /**
     * Delete an organization and cancel all its opportunities
     * 
     * @param organizationId The ID of the organization to delete
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> deleteOrganization(String organizationId) {
        return organizationRepo.findById(organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization not found")))
                .flatMap(organization -> {
                    // First, cancel all opportunities for this organization
                    return opportunityStatusService.cancelOpportunitiesForDeletedOrganization(organizationId)
                            .then(organizationRepo.deleteById(organizationId));
                })
                .doOnSuccess(result -> System.out.println("Successfully deleted organization: " + organizationId))
                .doOnError(error -> System.err
                        .println("Error deleting organization " + organizationId + ": " + error.getMessage()));
    }

    /**
     * Get organization statistics including active requests, total volunteers,
     * pending reviews, and completed tasks
     * 
     * @param organizationId The ID of the organization
     * @return Mono<Map<String, Object>> containing the stats
     */
    public Mono<Map<String, Object>> getOrganizationStats(String organizationId) {
        return organizationRepo.findById(organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization not found")))
                .flatMap(organization -> {
                    // Get all opportunities for this organization once to avoid multiple DB calls
                    return opportunityRepository.findByOrganizationId(organizationId)
                            .collectList()
                            .flatMap(opportunities -> {
                                Map<String, Object> stats = new HashMap<>();

                                // Count active requests
                                long activeRequests = opportunities.stream()
                                        .filter(opp -> "active".equals(opp.getStatus()))
                                        .count();

                                // Count completed tasks
                                long completedTasks = opportunities.stream()
                                        .filter(opp -> "completed".equals(opp.getStatus()))
                                        .count();

                                // Get all applications for these opportunities
                                if (opportunities.isEmpty()) {
                                    stats.put("activeRequests", activeRequests);
                                    stats.put("totalVolunteers", 0L);
                                    stats.put("pendingReviews", 0L);
                                    stats.put("completedTasks", completedTasks);
                                    return Mono.just(stats);
                                }

                                // Get applications for all opportunities of this organization
                                return Flux.fromIterable(opportunities)
                                        .flatMap(opp -> applicationRepository
                                                .findByOpportunityId(opp.getOpportunityId()))
                                        .collectList()
                                        .map(applications -> {
                                            // Count unique volunteers (by email/leaderEmail)
                                            long totalVolunteers = applications.stream()
                                                    .map(app -> "individual".equals(app.getType()) ? app.getEmail()
                                                            : app.getLeaderEmail())
                                                    .distinct()
                                                    .count();

                                            // Count pending reviews
                                            long pendingReviews = applications.stream()
                                                    .filter(app -> "pending".equals(app.getStatus()))
                                                    .count();

                                            stats.put("activeRequests", activeRequests);
                                            stats.put("totalVolunteers", totalVolunteers);
                                            stats.put("pendingReviews", pendingReviews);
                                            stats.put("completedTasks", completedTasks);
                                            return stats;
                                        });
                            });
                })
                .onErrorResume(error -> {
                    // Return default stats if there's an error
                    Map<String, Object> defaultStats = new HashMap<>();
                    defaultStats.put("activeRequests", 0L);
                    defaultStats.put("totalVolunteers", 0L);
                    defaultStats.put("pendingReviews", 0L);
                    defaultStats.put("completedTasks", 0L);
                    return Mono.just(defaultStats);
                });
    }

    /**
     * Get opportunities for an organization with application counts
     * 
     * @param organizationId The ID of the organization
     * @return Mono<List<Map<String, Object>>> containing opportunities with
     *         application data
     */
    public Mono<java.util.List<java.util.Map<String, Object>>> getOrganizationOpportunities(String organizationId) {
        return organizationRepo.findById(organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization not found")))
                .flatMap(organization -> {
                    return opportunityRepository.findByOrganizationId(organizationId)
                            .flatMap(opportunity -> {
                                // Get application count for each opportunity
                                return applicationRepository.findByOpportunityId(opportunity.getOpportunityId())
                                        .collectList()
                                        .map(applications -> {
                                            java.util.Map<String, Object> opportunityData = new java.util.HashMap<>();
                                            opportunityData.put("id", opportunity.getOpportunityId());
                                            opportunityData.put("title", opportunity.getTitle());
                                            opportunityData.put("description", opportunity.getDescription());
                                            opportunityData.put("status", opportunity.getStatus());
                                            opportunityData.put("volunteersNeeded", opportunity.getVolunteersNeeded());
                                            opportunityData.put("applicantCount", applications.size());

                                            // Count applications by status
                                            long pendingCount = applications.stream()
                                                    .filter(app -> "pending".equals(app.getStatus()))
                                                    .count();
                                            long approvedCount = applications.stream()
                                                    .filter(app -> "approved".equals(app.getStatus()))
                                                    .count();

                                            opportunityData.put("pendingApplications", pendingCount);
                                            opportunityData.put("approvedApplications", approvedCount);

                                            // Add dates if available
                                            if (opportunity.getStartDate() != null) {
                                                opportunityData.put("startDate",
                                                        opportunity.getStartDate().toDate().getTime());
                                            }
                                            if (opportunity.getEndDate() != null) {
                                                opportunityData.put("endDate",
                                                        opportunity.getEndDate().toDate().getTime());
                                            }
                                            if (opportunity.getDeadline() != null) {
                                                opportunityData.put("deadline",
                                                        opportunity.getDeadline().toDate().getTime());
                                            }

                                            return opportunityData;
                                        });
                            })
                            .collectList();
                })
                .onErrorResume(error -> {
                    System.err.println("Error fetching opportunities for organization " + organizationId + ": "
                            + error.getMessage());
                    return Mono.just(java.util.Collections.emptyList());
                });
    }

    /**
     * Get the total number of approved organizations
     * 
     * @return Mono<Long> containing the count of approved organizations
     */
    public Mono<Long> getTotalApprovedOrganizations() {
        return organizationRepo.findAll()
                .filter(organization -> "active".equals(organization.getStatus()))
                .count()
                .onErrorResume(error -> {
                    System.err.println("Error counting approved organizations: " + error.getMessage());
                    return Mono.just(0L);
                });
    }
}
