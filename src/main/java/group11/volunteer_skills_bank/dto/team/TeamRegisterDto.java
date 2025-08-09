package group11.volunteer_skills_bank.dto.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TeamRegisterDto(
        String firstName,
        String lastName,
        String email,
        String location,
        String interests,
        String reason,
        String teamId
) {
    @JsonCreator
    public TeamRegisterDto(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email,
            @JsonProperty("location") String location,
            @JsonProperty("interests") String interests,
            @JsonProperty("reason") String reason,
            @JsonProperty("teamId") String teamId
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.location = location;
        this.interests = interests;
        this.reason = reason;
        this.teamId = teamId;
    }
}