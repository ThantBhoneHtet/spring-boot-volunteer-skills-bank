package group11.volunteer_skills_bank.dto.opportunity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpportunityApprovalRequest {
    @NotBlank
    private String opportunityId;
    
    @NotNull
    private String approvalStatus; // "approved", "rejected"
    
    private String adminNotes; // Optional notes from admin
}
