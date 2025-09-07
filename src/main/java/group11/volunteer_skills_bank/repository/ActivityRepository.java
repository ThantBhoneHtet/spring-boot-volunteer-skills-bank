package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.Activity;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ActivityRepository extends FirestoreReactiveRepository<Activity> {

    /**
     * Find activities by type
     */
    Flux<Activity> findByType(String type);

    /**
     * Find activities by user ID
     */
    Flux<Activity> findByUserId(String userId);

    /**
     * Find activities by admin ID
     */
    Flux<Activity> findByAdminId(String adminId);

    /**
     * Count activities by type
     */
    Mono<Long> countByType(String type);
}
