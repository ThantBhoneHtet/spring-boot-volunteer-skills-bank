
package group11.volunteer_skills_bank.dto.team;

import com.google.cloud.Timestamp;
import group11.volunteer_skills_bank.model.Volunteer; // Import your Volunteer model
import lombok.Data;

import java.util.List;

@Data
public class TeamDetailsResponseDto {
    private String teamId;
    private String name;
    private String description;
    private List<Volunteer> members; 
    private List<Volunteer> leaders; 
    private String creatorId;
    private Timestamp createdAt;
    
}