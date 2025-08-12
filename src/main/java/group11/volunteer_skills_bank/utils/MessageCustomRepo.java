package group11.volunteer_skills_bank.utils;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QuerySnapshot;

import group11.volunteer_skills_bank.model.Message;
import reactor.core.publisher.Mono;

@Repository
public class MessageCustomRepo {
    private final Firestore firestore;
    private final String COLLECTION_NAME = "messages";

    public MessageCustomRepo(Firestore firestore) {
        this.firestore = firestore;
    }

    public Message findLatestMessage(String chatId) {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("chatId", chatId)
            .orderBy("sentAt", Direction.DESCENDING)
            .limit(1)
            .get();
        try {
            QueryDocumentSnapshot documentSnapshot = future.get().getDocuments().get(0);
            Message message = documentSnapshot.toObject(Message.class);
            return message;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching latest message", e); 
        }
    }

    public List<Message> getUnreadMessagesInChatByReaderId(String chatId, String readerId) {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("chatId", chatId)
            .get();

        try{
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            List<Message> unreadMessages = docs.stream()
            .map(doc -> doc.toObject(Message.class))
            .filter(msg -> !msg.getReadBy().contains(readerId))
            .toList();
            return unreadMessages;
        } catch(Exception e) {
            throw new RuntimeException("Error fetching unread messages", e);
        }
    }
}
