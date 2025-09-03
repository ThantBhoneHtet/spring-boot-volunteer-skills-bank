package group11.volunteer_skills_bank.service;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.repository.OpportunityRepository;
import reactor.core.publisher.Mono;

@Service
public class OpportunityStatusService {

    private static final Logger logger = Logger.getLogger(OpportunityStatusService.class.getName());
    
    private final OpportunityRepository opportunityRepository;

    @Value("${opportunity.status.update.enabled:true}")
    private boolean statusUpdateEnabled;

    @Value("${opportunity.status.update.interval:3600000}")
    private long updateInterval;

    @Autowired
    public OpportunityStatusService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    /**
     * Scheduled task that runs every hour to check for expired opportunities
     * and update their status to "complete"
     */
    @Scheduled(fixedRateString = "${opportunity.status.update.interval:3600000}")
    public void updateExpiredOpportunities() {
        if (!statusUpdateEnabled) {
            logger.info("Opportunity status updates are disabled");
            return;
        }

        logger.info("Starting scheduled task to update expired opportunities");
        
        Timestamp now = Timestamp.now();
        
        opportunityRepository.findAll()
                .filter(opportunity -> {
                    // Check if opportunity is active and has passed end date
                    return "active".equals(opportunity.getStatus()) && 
                           opportunity.getEndDate() != null && 
                           opportunity.getEndDate().compareTo(now) < 0;
                })
                .flatMap(opportunity -> {
                    logger.info("Updating expired opportunity: " + opportunity.getOpportunityId() + 
                              " from " + opportunity.getStatus() + " to complete");
                    
                    opportunity.setStatus("complete");
                    opportunity.setUpdatedAt(Timestamp.now());
                    
                    return opportunityRepository.save(opportunity);
                })
                .doOnComplete(() -> logger.info("Completed updating expired opportunities"))
                .doOnError(error -> logger.severe("Error updating expired opportunities: " + error.getMessage()))
                .subscribe();
    }

    /**
     * Update opportunities to cancelled status when their organization is deleted
     * @param organizationId The ID of the deleted organization
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> cancelOpportunitiesForDeletedOrganization(String organizationId) {
        logger.info("Cancelling opportunities for deleted organization: " + organizationId);
        
        return opportunityRepository.findByOrganizationId(organizationId)
                .filter(opportunity -> "active".equals(opportunity.getStatus()) || 
                                      "pending".equals(opportunity.getStatus()))
                .flatMap(opportunity -> {
                    logger.info("Cancelling opportunity: " + opportunity.getOpportunityId() + 
                              " due to organization deletion");
                    
                    opportunity.setStatus("cancelled");
                    opportunity.setUpdatedAt(Timestamp.now());
                    
                    return opportunityRepository.save(opportunity);
                })
                .then()
                .doOnSuccess(result -> logger.info("Successfully cancelled opportunities for organization: " + organizationId))
                .doOnError(error -> logger.severe("Error cancelling opportunities for organization " + 
                                               organizationId + ": " + error.getMessage()));
    }

    /**
     * Get the current configuration status
     * @return String describing the current configuration
     */
    public String getConfigurationStatus() {
        return String.format("Status updates enabled: %s, Interval: %d ms (%d minutes)", 
                           statusUpdateEnabled, updateInterval, updateInterval / 60000);
    }
}
