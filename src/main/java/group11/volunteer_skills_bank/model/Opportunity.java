package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;
import java.util.List;

@Data
@Document(collectionName = "opportunities")
public class Opportunity {
    @DocumentId
    private String opportunityId;
    private String title;
    private String description;
    private String organizationId;
    private List<String> skillsRequired;
    private Location location;
    private Timestamp startDate;
    private Timestamp endDate;
    private String timeCommitment;
    private Integer volunteersNeeded;
    private String status; // "active" | "completed" | "cancelled"
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<String> categories;

    @Data
    public static class Location {
        private String address;
        private String city;
        private GeoPoint coordinates;
        private Boolean remote;
    }
}