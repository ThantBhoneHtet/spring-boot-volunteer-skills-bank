package group11.volunteer_skills_bank.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.model.Opportunity;
import group11.volunteer_skills_bank.repository.OpportunityRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    public OpportunityService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    public Mono<Opportunity> create(Opportunity opportunity) {
        opportunity.setOpportunityId(UUID.randomUUID().toString());
        
        // Set creation timestamp
        Timestamp now = Timestamp.now();
        opportunity.setCreatedAt(now);
        opportunity.setUpdatedAt(now);
        
        // Set default status if not provided
        if (opportunity.getStatus() == null) {
            opportunity.setStatus("active");
        }
        if (opportunity.getApprovalStatus() == null) {
            opportunity.setApprovalStatus("pending");
        }
        
        // If no deadline is set, set it to 30 days from now by default
        if (opportunity.getDeadline() == null) {
            long thirtyDaysFromNow = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
            opportunity.setDeadline(Timestamp.ofTimeSecondsAndNanos(thirtyDaysFromNow / 1000, 0));
        }
        
        return opportunityRepository.save(opportunity);
    }

    public Mono<Opportunity> update(String id, Opportunity update) {
        return opportunityRepository.findById(id)
                .flatMap(existing -> {
                    // Security check: Only organization owners can edit their own posts
                    // This prevents admins from editing posts
                    if (existing.getOrganizationId() == null || update.getOrganizationId() == null) {
                        return Mono.error(new SecurityException("Organization ID is required for post updates"));
                    }
                    
                    // Only allow updates if the organization ID matches (same organization)
                    if (!existing.getOrganizationId().equals(update.getOrganizationId())) {
                        return Mono.error(new SecurityException("Only organization owners can edit their own posts"));
                    }
                    
                    existing.setTitle(update.getTitle());
                    existing.setDescription(update.getDescription());
                    existing.setSkillsRequired(update.getSkillsRequired());
                    existing.setCategories(update.getCategories());
                    existing.setTimeCommitment(update.getTimeCommitment());
                    existing.setVolunteersNeeded(update.getVolunteersNeeded());
                    existing.setType(update.getType());
                    existing.setLocation(update.getLocation());
                    existing.setStartDate(update.getStartDate());
                    existing.setEndDate(update.getEndDate());
                    existing.setDeadline(update.getDeadline());
                    
                    // Update the updatedAt timestamp
                    existing.setUpdatedAt(Timestamp.now());
                    
                    return opportunityRepository.save(existing);
                });
    }

    public Mono<Opportunity> updateStatus(String id, String newStatus) {
        return opportunityRepository.findById(id)
                .flatMap(existing -> {
                    existing.setStatus(newStatus);
                    existing.setUpdatedAt(Timestamp.now());
                    return opportunityRepository.save(existing);
                });
    }

    public Mono<Void> delete(String id) {
        return opportunityRepository.deleteById(id);
    }

    public Mono<Opportunity> getById(String id) {
        return opportunityRepository.findById(id);
    }

    public Flux<Opportunity> listAll() {
        return opportunityRepository.findAll();
    }

    public Flux<Opportunity> listByOrganization(String organizationId) {
        return opportunityRepository.findByOrganizationId(organizationId);
    }

    public Flux<Opportunity> getRecommended(String volunteerId) {
        // For now, return all active opportunities
        // In a real implementation, this would filter based on volunteer skills and preferences
        return opportunityRepository.findAll()
                .filter(opportunity -> "active".equals(opportunity.getStatus()))
                .take(10); // Limit to 10 recommendations
    }

    public Mono<Opportunity> approveOpportunity(String opportunityId, String approvalStatus, String adminNotes) {
        return opportunityRepository.findById(opportunityId)
                .flatMap(opportunity -> {
                    opportunity.setApprovalStatus(approvalStatus);
                    if ("approved".equals(approvalStatus)) {
                        opportunity.setStatus("active");
                    } else if ("rejected".equals(approvalStatus)) {
                        opportunity.setStatus("rejected");
                    }
                    
                    // Update the updatedAt timestamp when approval status changes
                    opportunity.setUpdatedAt(Timestamp.now());
                    
                    return opportunityRepository.save(opportunity);
                })
                .onErrorResume(error -> {
                    System.err.println("Error approving opportunity: " + error.getMessage());
                    return Mono.error(error);
                });
    }

    public Flux<Opportunity> getPendingApproval() {
        return opportunityRepository.findAll()
                .filter(opportunity -> "pending".equals(opportunity.getApprovalStatus()))
                .onErrorResume(error -> {
                    System.err.println("Error fetching pending opportunities: " + error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<Opportunity> getApprovedOpportunities() {
        return opportunityRepository.findAll()
                .filter(opportunity -> "approved".equals(opportunity.getApprovalStatus()))
                .onErrorResume(error -> {
                    System.err.println("Error fetching approved opportunities: " + error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<Opportunity> getRejectedOpportunities() {
        return opportunityRepository.findAll()
                .filter(opportunity -> "rejected".equals(opportunity.getApprovalStatus()))
                .onErrorResume(error -> {
                    System.err.println("Error fetching rejected opportunities: " + error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<Opportunity> getActiveOpportunities() {
        return opportunityRepository.findAll()
                .filter(opportunity -> "active".equals(opportunity.getStatus()) && 
                                     "approved".equals(opportunity.getApprovalStatus()))
                .onErrorResume(error -> {
                    System.err.println("Error fetching active opportunities: " + error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<String> getAllUniqueSkills() {
        return opportunityRepository.findAll()
                .doOnNext(opportunity -> {
                    System.out.println("Found opportunity: " + opportunity.getTitle() + 
                                     ", Status: " + opportunity.getStatus() + 
                                     ", ApprovalStatus: " + opportunity.getApprovalStatus() +
                                     ", Skills: " + opportunity.getSkillsRequired());
                })
                // Remove status filters to get all skills from database
                .flatMap(opportunity -> {
                    if (opportunity.getSkillsRequired() != null && !opportunity.getSkillsRequired().isEmpty()) {
                        return Flux.fromIterable(opportunity.getSkillsRequired());
                    }
                    return Flux.empty();
                })
                .distinct()
                .sort()
                .doOnNext(skill -> System.out.println("Returning skill: " + skill))
                .onErrorResume(error -> {
                    System.err.println("Error fetching skills: " + error.getMessage());
                    error.printStackTrace();
                    return Flux.empty();
                });
    }

    // Check if opportunity is available for applications
    public Mono<Object> checkAvailability(String opportunityId) {
        return opportunityRepository.findById(opportunityId)
                .map(opportunity -> {
                    final boolean available = opportunity.getVolunteersNeeded() > 0 && 
                                            "active".equals(opportunity.getStatus());
                    final int needed = opportunity.getVolunteersNeeded();
                    final String oppStatus = opportunity.getStatus();
                    
                    return new Object() {
                        public boolean isAvailable = available;
                        public int volunteersNeeded = needed;
                        public String status = oppStatus;
                    };
                })
                .cast(Object.class)
                .switchIfEmpty(Mono.fromCallable(() -> new Object() {
                    public boolean isAvailable = false;
                    public String error = "Opportunity not found";
                }));
    }
}


