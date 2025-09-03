package group11.volunteer_skills_bank.model;

import java.util.List;
import java.util.Map;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "feedbacks")
public class Feedback {
    @DocumentId
    private String feedbackId; // revieweeId
    private List<FeedbackDetails> feedbackDetialsList;

    private float averageScore;          // simple average
    private float weightedAverageScore;  // confidence-weighted average

    @Data
    public static class FeedbackDetails {
        private String opportunityId;
        private String reviewerId;
        private Timestamp feedbackTime;
        private String feedbackText;
        private int score;
        private double confidence;
    }
}
