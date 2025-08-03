package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.OrganizationRequest;
import reactor.core.publisher.Mono;

@Repository
public interface OrganizationRequestRepo extends FirestoreReactiveRepository<OrganizationRequest> 
{
    Mono<Boolean> existsByEmail(String email);
    Mono<OrganizationRequest> findByOrganizationId(String organizationId);    
} 