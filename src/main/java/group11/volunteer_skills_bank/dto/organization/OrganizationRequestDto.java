package group11.volunteer_skills_bank.dto.organization;

public record OrganizationRequestDto(
    String organizationId,
    String email,
    String organizationName,
    String organizationType,
    String password,
    String description,
    Location location
) 

{
    public record Location(
    String country,
    String city
    ){}
}
