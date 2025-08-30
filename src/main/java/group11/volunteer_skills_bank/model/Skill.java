package group11.volunteer_skills_bank.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "skill")
public class Skill {
    @DocumentId
    private String skillId;
    private String skill_name;
    private String categoryId;
    
    // Explicit getters/setters to avoid issues when Lombok annotation processing is unavailable
    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getSkill_name() {
        return skill_name;
    }

    public void setSkill_name(String skill_name) {
        this.skill_name = skill_name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
}