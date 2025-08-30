package group11.volunteer_skills_bank.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank
    private String categoryName;
}


