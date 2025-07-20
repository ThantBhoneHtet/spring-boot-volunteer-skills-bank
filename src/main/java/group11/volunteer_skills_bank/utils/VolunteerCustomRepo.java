package group11.volunteer_skills_bank.utils;

import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

@Repository
public class VolunteerCustomRepo {
    private final Firestore firestore;

    public VolunteerCustomRepo(Firestore firestore) {
        this.firestore = firestore;
    }

    public boolean existsByEmail(String email) {
        boolean flag = false;
        try {
            flag = firestore.collection("volunteers").whereEqualTo("email", email).get().get().isEmpty();
        } catch (Exception e) {
            return false;
        }
        return !(flag);
    }

    public DocumentReference getDocumentReference(String id) {
        return firestore.collection("volunteers").document(id);
    }

}
