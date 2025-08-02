package group11.volunteer_skills_bank.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import group11.volunteer_skills_bank.model.PasswordResetToken;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends FirestoreReactiveRepository<PasswordResetToken> {
}
