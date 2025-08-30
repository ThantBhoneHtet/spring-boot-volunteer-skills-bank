package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.Skill;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SkillRepository extends FirestoreReactiveRepository<Skill> {
    Flux<Skill> findByCategoryId(String categoryId);
}