package group11.volunteer_skills_bank.dto.team;

import java.util.List;

public record TeamCreationDto(
    String name,
    String description,
    List<String> members,
    List<String> leaders,
    String creatorId
) {}
