package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.Team;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends FirestoreReactiveRepository<Team> {
	
}