package group11.volunteer_skills_bank.dto.application;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {
    
    @NotBlank(message = "Status is required")
    private String status; // "pending", "approved", "rejected"
    
    private String adminNotes; // Optional notes from admin
}
