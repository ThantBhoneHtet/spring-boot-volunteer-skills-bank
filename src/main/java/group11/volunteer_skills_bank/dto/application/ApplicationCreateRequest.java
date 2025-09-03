package group11.volunteer_skills_bank.dto.application;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationCreateRequest {
    
    // Remove @NotBlank from volunteerId
    private String volunteerId; // <-- Add this field

    @NotBlank(message = "Opportunity ID is required")
    private String opportunityId;
    
    @NotBlank(message = "Application type is required")
    private String type; // "individual" or "team"
    
    // Individual fields
    private String fullName;
    private String dateOfBirth;
    
    // Remove @Email validation since this will store volunteerId
    private String email;
    
    private String phone;
    private String address;
    private List<String> skills; // Changed to List to support multiple skills
    
    // Team fields
    private String teamName;
    
    // Remove @Email validation since this field now stores volunteerId
    private String leaderEmail;
    
    private Integer members;
}
