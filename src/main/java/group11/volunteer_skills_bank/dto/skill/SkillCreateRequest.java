package group11.volunteer_skills_bank.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillCreateRequest {
    @NotBlank
    private String skillName;

    @NotNull
    private String categoryId;
}


