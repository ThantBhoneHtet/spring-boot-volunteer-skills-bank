package group11.volunteer_skills_bank.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import group11.volunteer_skills_bank.model.TeamApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TeamApprovalRepository {

    private final Firestore firestore;

    @Autowired
    public TeamApprovalRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Mono<TeamApproval> saveTeamApproval(TeamApproval approval) {
        System.out.println("TeamApprovalRepository: Attempting to save approval: " + approval);
        return Mono.fromCallable(() -> {
            if (approval == null || approval.getApprovalId() == null) {
                throw new IllegalArgumentException("TeamApproval or approvalId cannot be null.");
            }
            DocumentReference docRef = firestore.collection("teamApproval").document(approval.getApprovalId());
            docRef.set(approval).get(); // Synchronous for simplicity in Mono
            System.out.println("TeamApprovalRepository: Successfully saved approval with ID: " + approval.getApprovalId());
            return approval;
        }).onErrorResume(e -> {
            System.err.println("TeamApprovalRepository: Error saving approval: " + e.getMessage());
            e.printStackTrace();
            return Mono.error(new RuntimeException("Failed to save team approval: " + e.getMessage(), e));
        });
    }
}