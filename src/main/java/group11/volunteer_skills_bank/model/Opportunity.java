package group11.volunteer_skills_bank.model;

import java.util.List;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "opportunities")
public class Opportunity {
    @DocumentId
    private String opportunityId;
    
    private String title;
    private String description;
    
    @PropertyName("organizationId")
    private String organizationId;
    
    @PropertyName("skillsRequired")
    private List<String> skillsRequired;
    
    @PropertyName("location")
    private Location location;
    
    private Timestamp startDate;        // First day to do volunteer work
    private Timestamp endDate;          // Final day to do volunteer work
    private String timeCommitment;
    
    @PropertyName("volunteersNeeded")
    private Integer volunteersNeeded;
     
    private List<String> categories;
    private String status = "active";
    private String approvalStatus = "pending";
    
    // Timestamps for tracking
    private Timestamp createdAt;        // When the post was created
    private Timestamp updatedAt;        // When the post was last updated (including approval)
    private Timestamp deadline;         // Last date to apply for volunteer position
    private String type; // individual or team
    // Custom deserializer for organizationId to handle both String and DocumentReference
    public void setOrganizationId(Object organizationId) {
        if (organizationId instanceof String) {
            this.organizationId = (String) organizationId;
        } else if (organizationId instanceof com.google.cloud.firestore.DocumentReference) {
            // Extract the document ID from the reference
            this.organizationId = ((com.google.cloud.firestore.DocumentReference) organizationId).getId();
        } else {
            this.organizationId = organizationId != null ? organizationId.toString() : null;
        }
    }
    
    // Explicit getters and setters for all fields
    public String getOpportunityId() { return opportunityId; }
    public void setOpportunityId(String opportunityId) { this.opportunityId = opportunityId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getOrganizationId() { return organizationId; }
    
    public List<String> getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(List<String> skillsRequired) { this.skillsRequired = skillsRequired; }
    
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    
    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    
    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    
    public String getTimeCommitment() { return timeCommitment; }
    public void setTimeCommitment(String timeCommitment) { this.timeCommitment = timeCommitment; }
    
    public Integer getVolunteersNeeded() { return volunteersNeeded; }
    public void setVolunteersNeeded(Integer volunteersNeeded) { this.volunteersNeeded = volunteersNeeded; }
    
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    // Location inner class
    public static class Location {
        private String address;
        private String city;
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }
}