package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.OpportunityMembers;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityMemberRepository extends FirestoreReactiveRepository<OpportunityMembers> {
    // No custom methods needed here, as we'll handle array-contains queries in the service
}