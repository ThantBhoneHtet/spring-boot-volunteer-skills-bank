package group11.volunteer_skills_bank.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import group11.volunteer_skills_bank.model.TeamRegisterTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TeamRegisterTrackRepository {

    private final Firestore firestore;

    @Autowired
    public TeamRegisterTrackRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Mono<TeamRegisterTrack> saveTeamRegisterTrack(TeamRegisterTrack track) {
        System.out.println("TeamRegisterTrackRepository: Attempting to save track: " + track);
        return Mono.fromCallable(() -> {
            if (track == null || track.getTrackId() == null) {
                throw new IllegalArgumentException("TeamRegisterTrack or trackId cannot be null.");
            }
            DocumentReference docRef = firestore.collection("teamRegisterTrack").document(track.getTrackId());
            docRef.set(track).get(); // Synchronous for simplicity in Mono
            System.out.println("TeamRegisterTrackRepository: Successfully saved track with ID: " + track.getTrackId());
            return track;
        }).onErrorResume(e -> {
            System.err.println("TeamRegisterTrackRepository: Error saving track: " + e.getMessage());
            e.printStackTrace();
            return Mono.error(new RuntimeException("Failed to save team register track: " + e.getMessage(), e));
        });
    }
}