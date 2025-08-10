package group11.volunteer_skills_bank.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationLoginResponse 
{
    private String id; // Or Long, UUID, depending on your ID type
    private String email;
    private String organizationName;
}
