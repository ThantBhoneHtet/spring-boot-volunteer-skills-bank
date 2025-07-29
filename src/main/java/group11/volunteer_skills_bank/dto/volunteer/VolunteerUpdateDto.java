package group11.volunteer_skills_bank.dto.volunteer;

import java.util.List;

public record VolunteerUpdateDto (
    String password,
    String firstName,
    String lastName,
    String avatarUrl,
    VolunteerLocation location,
    String bio,
    List<String> skills,
    List<String> certifications,
    VolunteerPrivacySettings privacySettings,
        List<String> availability,
    String phoneNumber
){
    public record VolunteerPrivacySettings(
        String profileVisibility,
        Boolean searchVisibility
    ) {}

    public record VolunteerLocation(
        String city,
        String state
    ) {}
}


