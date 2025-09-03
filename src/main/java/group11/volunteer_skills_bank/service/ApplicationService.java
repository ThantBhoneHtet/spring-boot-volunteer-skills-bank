package group11.volunteer_skills_bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import group11.volunteer_skills_bank.dto.application.ApplicationCreateRequest;
import group11.volunteer_skills_bank.model.Application;
import group11.volunteer_skills_bank.repository.ApplicationRepository;
import group11.volunteer_skills_bank.repository.OpportunityRepository;
import group11.volunteer_skills_bank.repository.TeamRepository;
import group11.volunteer_skills_bank.service.TeamService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityMembersService opportunityMembersService;
    private final VolunteerService volunteerService;
    private final TeamService teamService;
    private final Firestore firestore;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                              OpportunityRepository opportunityRepository,
                              OpportunityMembersService opportunityMembersService,
                              VolunteerService volunteerService,
                              TeamService teamService,
                              Firestore firestore) {
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.opportunityMembersService = opportunityMembersService;
        this.volunteerService = volunteerService;
        this.teamService = teamService;
        this.firestore = firestore;
    }

    /**
     * Creates an application with auto-generated ID and validates for duplicates.
     *
     * @param request The application creation request.
     * @return A Mono containing the created application with generated ID.
     */
    public Mono<Application> createWithOpportunityValidation(ApplicationCreateRequest request) {
        System.out.println("ApplicationService: Creating application");
        String type = request.getType();
        String field = "individual".equals(type) ? "email" : "leaderEmail";
        String value = "individual".equals(type) ? request.getEmail() : request.getLeaderEmail();
        if (value == null || value.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException(field + " is required"));
        }
        String normalizedValue = value.trim().toLowerCase();

        // Resolve volunteerId using synchronous findVolunteerByEmail wrapped in Mono
        return Mono.fromCallable(() -> volunteerService.findVolunteerByEmail(normalizedValue))
                .flatMap(volunteer -> {
                    if (volunteer == null) {
                        return Mono.error(new IllegalArgumentException("Volunteer not found for " + field + ": " + normalizedValue));
                    }
                    return opportunityRepository.findById(request.getOpportunityId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Opportunity not found with ID: " + request.getOpportunityId())))
                            .flatMap(opportunity -> {
                                // Validate opportunity type matches application type
                                if ("individual".equals(opportunity.getType()) && "team".equals(request.getType())) {
                                    return Mono.error(new IllegalArgumentException("This opportunity only accepts individual applications"));
                                }
                                if ("team".equals(opportunity.getType()) && "individual".equals(request.getType())) {
                                    return Mono.error(new IllegalArgumentException("This opportunity only accepts team applications"));
                                }
                                
                                // Additional validation for team applications: check if volunteer is a team leader
                                if ("team".equals(opportunity.getType()) && "team".equals(request.getType())) {
                                    // First check if this team opportunity already has an approved team application
                                    return Mono.fromCallable(() -> {
                                        QuerySnapshot approvedTeamApps = firestore.collection("applicationRequest")
                                                .whereEqualTo("opportunityId", request.getOpportunityId())
                                                .whereEqualTo("type", "team")
                                                .whereEqualTo("status", "approved")
                                                .get().get();
                                        
                                        if (!approvedTeamApps.isEmpty()) {
                                            throw new IllegalStateException("OPPORTUNITY_CLOSED: This team opportunity already has an approved team application and is no longer accepting new applications.");
                                        }
                                        return true;
                                    }).flatMap(canProceed -> 
                                        teamService.validateVolunteerTeamLeadership(volunteer.getVolunteerId(), request.getTeamName())
                                                .flatMap(validationResult -> {
                                                    switch (validationResult) {
                                                        case "team_not_found":
                                                            return Mono.error(new IllegalArgumentException(
                                                                "TEAM_NOT_FOUND: The team '" + request.getTeamName() + 
                                                                "' does not exist. Please first create the team before applying."
                                                            ));
                                                        case "not_leader":
                                                            return Mono.error(new IllegalArgumentException(
                                                                "NOT_LEADER: You cannot apply because you are not a leader of the team '" + 
                                                                request.getTeamName() + "'. Only team leaders can apply for team opportunities."
                                                            ));
                                                        case "valid":
                                                            return proceedWithApplicationCreation(request, opportunity, volunteer, type);
                                                        default:
                                                            return Mono.error(new IllegalArgumentException(
                                                                "VALIDATION_ERROR: Unable to validate team leadership. Please try again."
                                                            ));
                                                    }
                                                })
                                    );
                                } else if ("individual".equals(opportunity.getType()) && "individual".equals(request.getType())) {
                                    // Check for duplicate individual applications from same volunteer to same opportunity
                                    return Mono.fromCallable(() -> {
                                        QuerySnapshot existingApps = firestore.collection("applicationRequest")
                                                .whereEqualTo("opportunityId", request.getOpportunityId())
                                                .whereEqualTo("type", "individual")
                                                .whereEqualTo("email", volunteer.getVolunteerId()) // email field stores volunteerId for individual apps
                                                .get().get();
                                        
                                        if (!existingApps.isEmpty()) {
                                            throw new IllegalStateException("DUPLICATE_APPLICATION: You have already applied to this opportunity. You cannot apply to the same opportunity multiple times.");
                                        }
                                        return true;
                                    }).flatMap(canProceed -> proceedWithApplicationCreation(request, opportunity, volunteer, type));
                                } else {
                                    return proceedWithApplicationCreation(request, opportunity, volunteer, type);
                                }
                            });
                });
    }

    /**
     * Helper method to proceed with application creation after all validations pass
     */
    private Mono<Application> proceedWithApplicationCreation(ApplicationCreateRequest request, 
                                                           Object opportunity, 
                                                           Object volunteer, 
                                                           String type) {
        return Mono.fromCallable(() -> {
            // Get opportunity and volunteer objects (they should be available from the calling context)
            String opportunityId = request.getOpportunityId();
            String volunteerId = getVolunteerIdFromVolunteer(volunteer);
            String organizationId = getOrganizationIdFromOpportunity(opportunity);
            
            QuerySnapshot querySnapshot = firestore.collection("applicationRequest")
                    .whereEqualTo("opportunityId", opportunityId)
                    .whereEqualTo("individual".equals(type) ? "email" : "leaderEmail", volunteerId)
                    .get().get();
            if (!querySnapshot.isEmpty()) {
                String field = "individual".equals(type) ? "email" : "leaderEmail";
                throw new IllegalStateException("You have already applied for this opportunity with this " + field);
            }
            return opportunity;
        }).flatMap(opp -> {
            Application application = new Application();
            application.setOrganizationId(getOrganizationIdFromOpportunity(opp));
            application.setOpportunityId(request.getOpportunityId());
            application.setType(type);
            
            String volunteerId = getVolunteerIdFromVolunteer(volunteer);
            
            if ("individual".equals(type)) {
                application.setFullName(request.getFullName());
                application.setDateOfBirth(request.getDateOfBirth());
                application.setEmail(volunteerId); // Store volunteerId in email field
                application.setPhone(request.getPhone());
                application.setAddress(request.getAddress());
                application.setSkills(request.getSkills());
            } else if ("team".equals(type)) {
                application.setTeamName(request.getTeamName());
                application.setLeaderEmail(volunteerId); // Store volunteerId in leaderEmail field
                application.setMembers(request.getMembers());
            } else {
                return Mono.error(new IllegalArgumentException("Invalid application type: " + type));
            }
            application.setSubmittedAt(Timestamp.now());
            application.setStatus("pending");
            System.out.println("ApplicationService: Saving application for opportunity: " + request.getOpportunityId());
            return applicationRepository.save(application);
        });
    }
    
    /**
     * Helper method to extract volunteerId from volunteer object
     */
    private String getVolunteerIdFromVolunteer(Object volunteer) {
        if (volunteer instanceof group11.volunteer_skills_bank.model.Volunteer) {
            return ((group11.volunteer_skills_bank.model.Volunteer) volunteer).getVolunteerId();
        }
        throw new IllegalArgumentException("Invalid volunteer object");
    }
    
    /**
     * Helper method to extract organizationId from opportunity object
     */
    private String getOrganizationIdFromOpportunity(Object opportunity) {
        if (opportunity instanceof group11.volunteer_skills_bank.model.Opportunity) {
            return ((group11.volunteer_skills_bank.model.Opportunity) opportunity).getOrganizationId();
        }
        throw new IllegalArgumentException("Invalid opportunity object");
    }

    /**
     * Retrieves an application by its generated ID.
     *
     * @param id The generated application ID.
     * @return A Mono containing the application.
     */
    public Mono<Application> getById(String id) {
        return applicationRepository.findById(id);
    }

    /**
     * Retrieves applications by organization ID.
     *
     * @param organizationId The organization ID.
     * @return A Flux of applications.
     */
    public Flux<Application> getByOrganizationId(String organizationId) {
        return applicationRepository.findByOrganizationId(organizationId);
    }

    /**
     * Retrieves applications by opportunity ID.
     *
     * @param opportunityId The opportunity ID.
     * @return A Flux of applications.
     */
    public Flux<Application> getByOpportunityId(String opportunityId) {
        return applicationRepository.findByOpportunityId(opportunityId);
    }

    /**
     * Approves an application if authorized by the organization.
     *
     * @param applicationId The generated application ID.
     * @param opportunityId The opportunity ID (optional, falls back to application's).
     * @param requestingOrgId The ID of the requesting organization.
     * @return A Mono containing the approved application.
     */
    public Mono<Application> approveApplication(String applicationId, String opportunityId, String requestingOrgId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Application not found with ID: " + applicationId)))
                .flatMap(application -> {
                    String oppId = opportunityId != null ? opportunityId : application.getOpportunityId();
                    if (oppId == null) {
                        return Mono.error(new IllegalArgumentException("Opportunity ID required for approval"));
                    }
                    return opportunityRepository.findById(oppId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Opportunity not found with ID: " + oppId)))
                            .flatMap(opportunity -> {
                                if (!requestingOrgId.equals(opportunity.getOrganizationId())) {
                                    return Mono.error(new IllegalStateException("Organization not authorized to approve this application"));
                                }
                                application.setStatus("approved");
                                application.setRespondedAt(Timestamp.now());
                                return applicationRepository.save(application)
                                        .flatMap(savedApp -> {
                                            if ("individual".equals(savedApp.getType())) {
                                                // Store volunteerId directly in OpportunityMembers.volunteers
                                                return opportunityMembersService.addVolunteerToOpportunity(
                                                        oppId, savedApp.getEmail(), opportunity.getOrganizationId())
                                                        .then(Mono.just(savedApp));
                                            } else if ("team".equals(savedApp.getType())) {
                                                // Retrieve teamId from team name
                                                return teamService.findTeamByName(savedApp.getTeamName())
                                                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                                "Team not found with name: " + savedApp.getTeamName())))
                                                        .flatMap(team -> {
                                                            // Use teamId in OpportunityMembers
                                                            return opportunityMembersService.addTeamToOpportunity(
                                                                    savedApp.getOpportunityId(),
                                                                    team.getTeamId(),
                                                                    opportunity.getOrganizationId())
                                                                    .thenReturn(savedApp);
                                                        });
                                            }
                                            // If neither individual nor team, just return the saved application
                                            return Mono.just(savedApp);
                                        })
                                        .flatMap(savedApp -> {
                                            // Update opportunity volunteers needed count only for individual applications
                                            // Team opportunities don't reduce volunteersNeeded since they accept one team regardless of size
                                            if ("individual".equals(savedApp.getType())) {
                                                return opportunityRepository.findById(oppId)
                                                        .flatMap(opp -> {
                                                            int currentNeeded = opp.getVolunteersNeeded();
                                                            if (currentNeeded > 0) {
                                                                opp.setVolunteersNeeded(currentNeeded - 1);
                                                                return opportunityRepository.save(opp).thenReturn(savedApp);
                                                            }
                                                            return Mono.just(savedApp);
                                                        });
                                            } else {
                                                // For team applications, don't modify volunteersNeeded
                                                return Mono.just(savedApp);
                                            }
                                        });
                            });
                });
    }

    /**
     * Rejects an application if authorized by the organization.
     *
     * @param applicationId The generated application ID.
     * @param reason The rejection reason.
     * @param requestingOrgId The ID of the requesting organization.
     * @return A Mono containing the rejected application.
     */
    public Mono<Application> rejectApplication(String applicationId, String reason, String requestingOrgId) {
        return applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Application not found with ID: " + applicationId)))
                .flatMap(application -> opportunityRepository.findById(application.getOpportunityId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Opportunity not found with ID: " + application.getOpportunityId())))
                        .flatMap(opportunity -> {
                            if (!requestingOrgId.equals(opportunity.getOrganizationId())) {
                                return Mono.error(new IllegalStateException("Organization not authorized to reject this application"));
                            }
                            application.setStatus("rejected");
                            application.setOrganizationResponse(reason);
                            application.setRespondedAt(Timestamp.now());
                            return applicationRepository.save(application);
                        }));
    }

    /**
     * Deletes an application by its generated ID.
     *
     * @param id The generated application ID.
     * @return A Mono of Void.
     */
    public Mono<Void> delete(String id) {
        return applicationRepository.deleteById(id);
    }

    /**
     * Retrieves applications with enriched volunteer details for a specific opportunity.
     * This method fetches volunteer information based on the volunteerId stored in email/leaderEmail fields.
     *
     * @param opportunityId The opportunity ID.
     * @return A Flux of enriched application data with volunteer details.
     */
    public Flux<java.util.Map<String, Object>> getApplicationsWithVolunteerDetails(String opportunityId) {
        return applicationRepository.findByOpportunityId(opportunityId)
                .flatMap(application -> {
                    // Get volunteerId from email field (for individual) or leaderEmail field (for team)
                    String volunteerId = "individual".equals(application.getType()) ? 
                            application.getEmail() : application.getLeaderEmail();
                    
                    return volunteerService.findVolunteerById(volunteerId)
                            .map(volunteer -> {
                                java.util.Map<String, Object> enrichedApplication = new java.util.HashMap<>();
                                
                                // Application basic info
                                enrichedApplication.put("applicationId", application.getApplicationId());
                                enrichedApplication.put("opportunityId", application.getOpportunityId());
                                enrichedApplication.put("organizationId", application.getOrganizationId());
                                enrichedApplication.put("type", application.getType());
                                enrichedApplication.put("status", application.getStatus());
                                enrichedApplication.put("submittedAt", application.getSubmittedAt());
                                enrichedApplication.put("respondedAt", application.getRespondedAt());
                                enrichedApplication.put("organizationResponse", application.getOrganizationResponse());
                                
                                // Application specific fields
                                if ("individual".equals(application.getType())) {
                                    enrichedApplication.put("fullName", application.getFullName());
                                    enrichedApplication.put("dateOfBirth", application.getDateOfBirth());
                                    enrichedApplication.put("phone", application.getPhone());
                                    enrichedApplication.put("address", application.getAddress());
                                    enrichedApplication.put("skills", application.getSkills());
                                } else if ("team".equals(application.getType())) {
                                    enrichedApplication.put("teamName", application.getTeamName());
                                    enrichedApplication.put("members", application.getMembers());
                                }
                                
                                // Volunteer details
                                enrichedApplication.put("volunteerId", volunteer.getVolunteerId());
                                enrichedApplication.put("volunteerEmail", volunteer.getEmail());
                                enrichedApplication.put("volunteerFirstName", volunteer.getFirstName());
                                enrichedApplication.put("volunteerLastName", volunteer.getLastName());
                                enrichedApplication.put("volunteerAvatarUrl", volunteer.getAvatarUrl());
                                enrichedApplication.put("volunteerBio", volunteer.getBio());
                                enrichedApplication.put("volunteerSkills", volunteer.getSkills());
                                enrichedApplication.put("volunteerCertifications", volunteer.getCertifications());
                                enrichedApplication.put("volunteerPhoneNumber", volunteer.getPhoneNumber());
                                enrichedApplication.put("volunteerLocation", volunteer.getLocation());
                                enrichedApplication.put("volunteerRating", volunteer.getRating());
                                enrichedApplication.put("volunteerTotalHours", volunteer.getTotalHours());
                                enrichedApplication.put("volunteerCompletedOpportunities", volunteer.getCompletedOpportunities());
                                enrichedApplication.put("volunteerIsOnline", volunteer.getIsOnline());
                                enrichedApplication.put("volunteerLastActive", volunteer.getLastActive());
                                
                                return enrichedApplication;
                            })
                            .switchIfEmpty(Mono.fromSupplier(() -> {
                                // Volunteer not found, create basic application info
                                java.util.Map<String, Object> enrichedApplication = new java.util.HashMap<>();
                                
                                // Application basic info
                                enrichedApplication.put("applicationId", application.getApplicationId());
                                enrichedApplication.put("opportunityId", application.getOpportunityId());
                                enrichedApplication.put("organizationId", application.getOrganizationId());
                                enrichedApplication.put("type", application.getType());
                                enrichedApplication.put("status", application.getStatus());
                                enrichedApplication.put("submittedAt", application.getSubmittedAt());
                                enrichedApplication.put("respondedAt", application.getRespondedAt());
                                enrichedApplication.put("organizationResponse", application.getOrganizationResponse());
                                
                                // Application specific fields
                                if ("individual".equals(application.getType())) {
                                    enrichedApplication.put("fullName", application.getFullName());
                                    enrichedApplication.put("dateOfBirth", application.getDateOfBirth());
                                    enrichedApplication.put("phone", application.getPhone());
                                    enrichedApplication.put("address", application.getAddress());
                                    enrichedApplication.put("skills", application.getSkills());
                                } else if ("team".equals(application.getType())) {
                                    enrichedApplication.put("teamName", application.getTeamName());
                                    enrichedApplication.put("members", application.getMembers());
                                }
                                
                                // Default volunteer info when not found
                                enrichedApplication.put("volunteerId", volunteerId);
                                enrichedApplication.put("volunteerEmail", "Unknown");
                                enrichedApplication.put("volunteerFirstName", "Unknown");
                                enrichedApplication.put("volunteerLastName", "Volunteer");
                                enrichedApplication.put("volunteerAvatarUrl", null);
                                enrichedApplication.put("volunteerBio", "Volunteer details not available");
                                enrichedApplication.put("volunteerSkills", java.util.List.of());
                                enrichedApplication.put("volunteerCertifications", java.util.List.of());
                                enrichedApplication.put("volunteerPhoneNumber", null);
                                enrichedApplication.put("volunteerLocation", null);
                                enrichedApplication.put("volunteerRating", 0.0);
                                enrichedApplication.put("volunteerTotalHours", 0);
                                enrichedApplication.put("volunteerCompletedOpportunities", 0);
                                enrichedApplication.put("volunteerIsOnline", false);
                                enrichedApplication.put("volunteerLastActive", null);
                                
                                return enrichedApplication;
                            }))
                            .onErrorResume(error -> {
                                // Handle error case
                                java.util.Map<String, Object> errorApplication = new java.util.HashMap<>();
                                errorApplication.put("applicationId", application.getApplicationId());
                                errorApplication.put("error", "Failed to load volunteer details: " + error.getMessage());
                                return Mono.just(errorApplication);
                            });
                });
    }
}