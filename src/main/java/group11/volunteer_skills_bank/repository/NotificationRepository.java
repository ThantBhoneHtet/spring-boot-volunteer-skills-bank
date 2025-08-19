package group11.volunteer_skills_bank.repository;

import com.google.cloud.firestore.*;
import group11.volunteer_skills_bank.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class NotificationRepository {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "notifications";

    public NotificationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String save(Notification notification) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        notification.setId(docRef.getId());
        docRef.set(notification).get();
        return docRef.getId();
    }

    public List<Notification> findByRecipientId(String recipientId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .whereEqualTo("recipientId", recipientId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(Notification.class))
                .collect(Collectors.toList());
    }

    public Notification findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(id).get().get();
        return document.exists() ? document.toObject(Notification.class) : null;
    }

    public void markAsRead(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME).document(id)
                .update("read", true).get();
    }
}