package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Data;
import java.util.List;

@Data
@Document(collectionName = "teams")
public class Team {
    @DocumentId
    private String teamId;

    private String name;
    private String description;
    private List<String> members; // user references
    private List<String> leaders; // user references
    private String creatorId; // Added to track the volunteer who created the team
    private Timestamp createdAt;
}