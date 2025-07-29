package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;

import group11.volunteer_skills_bank.model.Chat;
import reactor.core.publisher.Flux;

public interface ChatRepository extends FirestoreReactiveRepository<Chat> {
    
    Flux<Chat> findByParticipantsContaining(String userId);
    
} 