package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Document(collectionName = "organizations")
public class Organization {
    @DocumentId
    private String organizationId;
    private String email;
    private String password;
    private String address;
    private String organizationName;
    private String organizationType;
    private String description;
    private String logoUrl;
    private String website;
    private String phoneNumber;
    private Location location;
    private Boolean verified;
    private List<String> verificationDocuments;
    private String status; // "active" | "suspended" | "pending"
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private Integer memberCount;
    private Integer opportunityCount;
    private Double rating;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location {
        private String city;
        private String country;
    }
}