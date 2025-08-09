package group11.volunteer_skills_bank.model;


import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Data;


@Data
@Document(collectionName = "teamApproval")
public class TeamApproval {
    @DocumentId
    private String approvalId;

    private String volunteerId;
    private boolean isAccept;
    private boolean isReject;
    private String teamId; 
    
}