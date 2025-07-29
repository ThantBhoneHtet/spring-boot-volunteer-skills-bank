package group11.volunteer_skills_bank.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

@Repository
public class ChatCustomRepo {

    private final Firestore firestore;
    private final String COLLECTION_NAME = "chats";

   

    public ChatCustomRepo(Firestore firestore) {
        this.firestore = firestore;
    }

    public String getChatIdBetweenParticipants(String participant1, String participant2) {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
            .whereArrayContains("participants", participant1)
            .whereArrayContains("participants", participant2)
            .get();
        try {
            QuerySnapshot querySnapshot = future.get();
            if (querySnapshot.isEmpty()) {
                return null;
            } else {
                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                return documentSnapshot.getId();
            }
        } catch (Exception e) {
            return null;
        }
    }

}
