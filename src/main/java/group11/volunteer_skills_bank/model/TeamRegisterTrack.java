package group11.volunteer_skills_bank.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Data;

@Data
@Document(collectionName = "teamRegisterTrack")
public class TeamRegisterTrack {
    @DocumentId
    private String trackId;

    private String status;
    private Timestamp approvalDate;
    private String approvalId;
    private String volunteerId; // Add this to link to the volunteer
    private String teamId;     // Add this to link to the team
}