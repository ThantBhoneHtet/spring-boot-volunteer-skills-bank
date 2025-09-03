package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;

import group11.volunteer_skills_bank.model.Application;
import reactor.core.publisher.Flux;

@Repository
public interface ApplicationRepository extends FirestoreReactiveRepository<Application> {
    
    // Find applications by organization ID
    Flux<Application> findByOrganizationId(String organizationId);
    
    // Find applications by opportunity ID
    Flux<Application> findByOpportunityId(String opportunityId);
    
    // Find applications by email (for individual applications)
    Flux<Application> findByEmail(String email);
    
    // Find applications by status
    Flux<Application> findByStatus(String status);
    
    // Find applications by type (individual or team)
    Flux<Application> findByType(String type);
}
