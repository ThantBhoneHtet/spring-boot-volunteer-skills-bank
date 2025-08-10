package group11.volunteer_skills_bank.dto.organization;

public record OrganizationUpdate(
        
        String organizationName,
        String organizationType,
        String discription,
        String website,
        String logoUrl,
        String phoneNumber,
        Location location
) {
     public record Location(
        String country,
        String city
    ) {}
}
