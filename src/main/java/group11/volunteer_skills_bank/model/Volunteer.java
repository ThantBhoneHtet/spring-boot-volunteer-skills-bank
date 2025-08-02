package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Document(collectionName = "volunteers")
public class Volunteer {
    @DocumentId
    private String volunteerId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Location location;
    private String bio;
    private List<String> skills;
    private List<String> certifications;
    private String phoneNumber;
    private String status; // "active" | "suspended"
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private PrivacySettings privacySettings;
    private List<String> availability;
    private Integer totalHours;
    private Integer completedOpportunities;
    private Double rating;
    private Timestamp lastActive;
    private Boolean isOnline;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location {
        private String coutry;
        private String city;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrivacySettings {
        private String profileVisibility; // "public" | "connections" | "private"
        private Boolean searchVisibility;
    }
}