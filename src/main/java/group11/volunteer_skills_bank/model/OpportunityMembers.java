package group11.volunteer_skills_bank.model;


import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;
import lombok.Data;
import java.util.List;

@Data
@Document(collectionName = "opportunityMembers")
public class OpportunityMembers {
	
	@DocumentId
    private String listId;
 
    private String opportunityId;
    private List<String> volunteers; 
    private List<String> teams; 
    private String organizationId;
  
	

}
