// File: src/main/java/group11/volunteer_skills_bank/dto/team/TeamRegisterDto.java

package group11.volunteer_skills_bank.dto.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TeamRegisterDto(
        String firstName,
        String lastName,
        String email,
        String location,
        List<String> skills, // Renamed 'interests' to 'skills' and changed type
        String reason,
        String teamId
) {
    @JsonCreator
    public TeamRegisterDto(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email,
            @JsonProperty("location") String location,
            @JsonProperty("skills") List<String> skills,
            @JsonProperty("reason") String reason,
            @JsonProperty("teamId") String teamId
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.location = location;
        this.skills = skills;
        this.reason = reason;
        this.teamId = teamId;
    }
}