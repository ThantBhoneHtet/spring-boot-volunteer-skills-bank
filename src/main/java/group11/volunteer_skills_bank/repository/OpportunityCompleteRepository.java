package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.Opportunity;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityCompleteRepository extends FirestoreReactiveRepository<Opportunity> {
    // No custom methods needed here, as we'll handle queries in the service
}