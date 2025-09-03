package group11.volunteer_skills_bank.dto.application;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationApprovalRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId; // Used to validate org making the decision

    @NotBlank(message = "Opportunity ID is required")
    private String opportunityId;  // Needed to reduce volunteersNeeded on approve

    private String reason;                // Optional, mainly for reject endpoint
    private String organizationResponse;  // Optional response/feedback from org
}
