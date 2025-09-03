package group11.volunteer_skills_bank.utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QuerySnapshot;

import group11.volunteer_skills_bank.model.Feedback;

@Repository
public class FeedbackCustomRepo {
    private final Firestore firestore;
    private final String COLLECTION_NAME = "feedbacks";

    public FeedbackCustomRepo(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Feedback> findTopRatedFeedbacks(
        List<String> skills, 
        Integer minCompletedOpportunities, 
        Float minRating) {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .orderBy("weightedAverageScore", Direction.DESCENDING)
                .limit(10)
                .get();
            try {
                List<Feedback> feedbacks = future.get().toObjects(Feedback.class);
                // Post-process: keep only top 3 feedbackDetails by score
                for (Feedback feedback : feedbacks) {
                    if (feedback.getFeedbackDetialsList() != null) {
                        List<Feedback.FeedbackDetails> topDetails = feedback.getFeedbackDetialsList().stream()
                                .sorted(Comparator.comparingInt(Feedback.FeedbackDetails::getScore).reversed())
                                .limit(3)
                                .collect(Collectors.toList());

                        feedback.setFeedbackDetialsList(topDetails);
                    }
                }
                return feedbacks;
            } catch (Exception e) {
                throw new RuntimeException("Error fetching top-rated feedbacks", e);
            }
    }
}
