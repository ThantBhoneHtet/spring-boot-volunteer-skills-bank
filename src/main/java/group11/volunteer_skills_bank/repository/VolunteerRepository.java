package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.Volunteer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VolunteerRepository extends FirestoreReactiveRepository<Volunteer> {

    Mono<Boolean> existsByEmail(String email);
    Flux<Volunteer> findByEmail(String email);
    
}
