package group11.volunteer_skills_bank.dto.volunteer;

import jakarta.validation.constraints.*;

public record VolunteerCreationDto(
    @Email
    String email,

    @NotBlank
    String password,
    
    @NotBlank
    String firstName,
    
    @NotBlank
    String lastName
) {}

