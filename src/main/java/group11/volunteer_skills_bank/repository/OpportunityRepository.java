package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;

import group11.volunteer_skills_bank.model.Opportunity;
import reactor.core.publisher.Flux;

@Repository
public interface OpportunityRepository extends FirestoreReactiveRepository<Opportunity> {
    
    // Custom method to find opportunities by organization ID
    Flux<Opportunity> findByOrganizationId(String organizationId);
}


