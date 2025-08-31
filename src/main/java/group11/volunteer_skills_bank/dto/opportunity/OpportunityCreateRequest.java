package group11.volunteer_skills_bank.dto.opportunity;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpportunityCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String organizationId;
    @NotNull
    private List<String> skillsRequired;
    private Location location;
    // Use epoch millis to decouple from Firestore types in the DTO layer
    private Long startDateMillis;      // First day to do volunteer work
    private Long endDateMillis;        // Final day to do volunteer work
    private Long deadlineMillis;       // Last date to apply for volunteer position
    private String timeCommitment;
    private Integer volunteersNeeded;
    private List<String> categories;
    private String type; // individual or team
    
    // Location DTO class
    public static class Location {
        private String address;
        private String city;
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }
}


