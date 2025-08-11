package group11.volunteer_skills_bank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import group11.volunteer_skills_bank.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class NotificationRepository {

    private final Firestore firestore;
    private final CollectionReference notificationsRef;

    @Autowired
    public NotificationRepository(Firestore firestore) {
        this.firestore = firestore;
        this.notificationsRef = firestore.collection("notifications");
    }

    public CompletableFuture<String> save(Notification notification) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        DocumentReference newRef = notificationsRef.document();
        notification.setId(newRef.getId());
        
        newRef.set(notification).addListener(() -> {
            future.complete(newRef.getId());
        }, Runnable::run);
        
        return future;
    }

    public CompletableFuture<List<Notification>> findByOrganizationId(String organizationId) {
        ApiFuture<QuerySnapshot> future = notificationsRef.whereEqualTo("organizationId", organizationId).get();
        return CompletableFuture.supplyAsync(() -> {
            try {
                QuerySnapshot snapshot = future.get(); // only one call
                return snapshot.getDocuments().stream()
                        .map(doc -> doc.toObject(Notification.class))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    public CompletableFuture<List<Notification>> findByTeamId(String teamId) {
        CompletableFuture<List<Notification>> future = new CompletableFuture<>();
        
        notificationsRef.whereEqualTo("teamId", teamId)
            .get()
            .addListener(() -> {
                try {
                    QuerySnapshot snapshot = notificationsRef.whereEqualTo("teamId", teamId).get().get();
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        notifications.add(document.toObject(Notification.class));
                    }
                    future.complete(notifications);
                } catch (InterruptedException | ExecutionException e) {
                    future.completeExceptionally(e);
                }
            }, Runnable::run);
        
        return future;
    }

    public CompletableFuture<Notification> findByIdAndMarkRead(String notificationId) {
        CompletableFuture<Notification> future = new CompletableFuture<>();
        
        notificationsRef.document(notificationId)
            .get()
            .addListener(() -> {
                try {
                    DocumentSnapshot document = notificationsRef.document(notificationId).get().get();
                    if (document.exists()) {
                        Notification notification = document.toObject(Notification.class);
                        notification.setRead(true);
                        
                        notificationsRef.document(notificationId)
                            .set(notification)
                            .addListener(() -> future.complete(notification), Runnable::run);
                    } else {
                        future.completeExceptionally(new RuntimeException("Notification not found"));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    future.completeExceptionally(e);
                }
            }, Runnable::run);
        
        return future;
    }
}