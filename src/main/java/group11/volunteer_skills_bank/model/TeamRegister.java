package group11.volunteer_skills_bank.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import java.util.List;

@Data
@Document(collectionName = "teamRegister")
public class TeamRegister {
    @DocumentId
    private String registerId;

    private String firstName;
    private String lastName;
    private String email;
    private String location;
    private List<String> skills;
    private String reason;
    private String teamId;
    private Timestamp createdAt;

    public TeamRegister() {}

    public TeamRegister(String registerId, String firstName, String lastName, String email, String location, List<String> skills, String reason, String teamId, Timestamp createdAt) {
        this.registerId = registerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.location = location;
        this.skills = skills;
        this.reason = reason;
        this.teamId = teamId;
        this.createdAt = createdAt != null ? createdAt : Timestamp.now();
    }
}