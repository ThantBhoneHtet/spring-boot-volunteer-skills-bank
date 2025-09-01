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
                .whereArrayContains("recipientIds", recipientId) // Use arrayContains for lists
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(Notification.class))
                .collect(Collectors.toList());
    }

    public List<Notification> findByRecipientIdAndRead(String recipientId, boolean read) 
            throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_NAME)
                .whereArrayContains("recipientIds", recipientId) // Use arrayContains
                .whereEqualTo("read", read)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(Notification.class))
                .collect(Collectors.toList());
    }

    public List<Notification> findWithFilters(String recipientId, String type, 
        Boolean unreadOnly, String priority) throws ExecutionException, InterruptedException {

        Query query = firestore.collection(COLLECTION_NAME)
                .whereArrayContains("recipientIds", recipientId) // Use arrayContains for lists
                .orderBy("timestamp", Query.Direction.DESCENDING);

        if (type != null && !type.equals("all")) {
            query = query.whereEqualTo("type", type);
        }

        if (unreadOnly != null && unreadOnly) {
            query = query.whereEqualTo("read", false);
        }

        return query.get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(Notification.class))
                .collect(Collectors.toList());
    }

    public void markAllAsRead(String recipientId) throws ExecutionException, InterruptedException {
        List<Notification> unreadNotifications = findByRecipientIdAndRead(recipientId, false);
        
        for (Notification notification : unreadNotifications) {
            markAsRead(notification.getId());
        }
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