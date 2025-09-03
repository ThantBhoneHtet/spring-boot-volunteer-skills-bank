package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "reviews")
public class Review {
    @DocumentId
    private String reviewId;
    private String reviewerId;
    private String revieweeId;
    private String opportunityId;
    private String type; // "org-to-volunteer" | "volunteer-to-org"
    private Integer rating; // 1-5
    private String comments;
    private Timestamp createdAt;
    private Boolean anonymous;
}
