package group11.volunteer_skills_bank.model;

import java.util.List;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collectionName = "applicationRequest")
public class Application {
    @DocumentId
    private String applicationId; 
    private String organizationId;
    private String opportunityId;
    private String type;// individual or team
    private Timestamp submittedAt;
    private String status;// pending, approved, rejected

    // Individual fields
    private String fullName;
    private String dateOfBirth;
    private String email;
    private String phone;
    private String address;
    private List<String> skills;

    // Team fields
    private String teamName;
    private String leaderEmail;
    private Integer members;

    // Organization response fields
    private String organizationResponse;
    private Timestamp respondedAt;

    // Getter and setter for skills
    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}