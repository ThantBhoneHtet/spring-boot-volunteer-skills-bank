package group11.volunteer_skills_bank.model;

import com.google.cloud.spring.data.firestore.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collectionName = "organizationRequests")
public class OrganizationRequest 
{
    @DocumentId
    private String organizationId;
    private String email;
    private String organizationName;
    private String organizationType;
    private String address;
    private String password;
    private String description;
    private String status;
    private Timestamp createdAt;
    private String rejectionReason;
}
