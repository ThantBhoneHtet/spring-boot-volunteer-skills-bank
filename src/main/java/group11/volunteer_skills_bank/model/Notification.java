package group11.volunteer_skills_bank.model;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Notification {
    private String id; // Firebase will generate this
    private String content;
    private String email; // recipient email
    private boolean read;
    private Date timestamp;
    private String title;
    private String type; // "individual_application", "team_application", "team_registration"
    
    // Metadata fields
    private String volunteerName;
    private String postName;
    private String teamName;
    private List<String> teamMembers;
    private String organizationId;
    private String teamId;
    private String leaderId;
}