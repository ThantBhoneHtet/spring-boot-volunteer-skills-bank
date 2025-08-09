package group11.volunteer_skills_bank.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import group11.volunteer_skills_bank.model.TeamRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TeamRegisterRepo {

    private final Firestore firestore;

    @Autowired
    public TeamRegisterRepo(Firestore firestore) {
        this.firestore = firestore;
    }

    public Mono<TeamRegister> saveTeamRegistration(TeamRegister registration) {
        System.out.println("TeamRegisterRepo: Attempting to save registration: " + registration);
        return Mono.fromCallable(() -> {
            if (registration == null) {
                throw new IllegalArgumentException("Registration cannot be null.");
            }
            DocumentReference docRef = firestore.collection("teamRegister").document();
            registration.setRegisterId(docRef.getId());
            System.out.println("TeamRegisterRepo: Setting registerId: " + docRef.getId() + ", createdAt: " + registration.getCreatedAt());
            docRef.set(registration).get();
            System.out.println("TeamRegisterRepo: Successfully saved registration with ID: " + registration.getRegisterId());
            return registration;
        }).onErrorResume(e -> {
            System.err.println("TeamRegisterRepo: Error saving registration: " + e.getMessage());
            e.printStackTrace();
            return Mono.error(new RuntimeException("Failed to save team registration: " + e.getMessage(), e));
        });
    }

    public Mono<TeamRegister> findByTeamIdAndVolunteerId(String teamId, String volunteerId) {
        System.out.println("TeamRegisterRepo: Checking for existing registration with teamId: " + teamId + ", volunteerId: " + volunteerId);
        return Mono.fromCallable(() -> {
            QuerySnapshot querySnapshot = firestore.collection("teamRegister")
                    .whereEqualTo("teamId", teamId)
                    .whereEqualTo("email", volunteerId)
                    .get()
                    .get();
            if (!querySnapshot.isEmpty()) {
                TeamRegister registration = querySnapshot.getDocuments().get(0).toObject(TeamRegister.class);
                System.out.println("TeamRegisterRepo: Found existing registration with registerId: " + registration.getRegisterId());
                return registration;
            }
            System.out.println("TeamRegisterRepo: No existing registration found for teamId: " + teamId + ", volunteerId: " + volunteerId);
            return null;
        }).onErrorResume(e -> {
            System.err.println("TeamRegisterRepo: Error checking registration for teamId " + teamId + ", volunteerId " + volunteerId + ": " + e.getMessage());
            return Mono.error(new RuntimeException("Failed to check registration: " + e.getMessage(), e));
        });
    }
}