package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;

import group11.volunteer_skills_bank.model.OpportunityMembers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OpportunityMembersRepository extends FirestoreReactiveRepository<OpportunityMembers> {
    
    Mono<OpportunityMembers> findByOpportunityId(String opportunityId);
    
    Flux<OpportunityMembers> findByOrganizationId(String organizationId);
}
