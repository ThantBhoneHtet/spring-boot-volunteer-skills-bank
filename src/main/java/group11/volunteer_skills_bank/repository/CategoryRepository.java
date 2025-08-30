package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends FirestoreReactiveRepository<Category> {

}