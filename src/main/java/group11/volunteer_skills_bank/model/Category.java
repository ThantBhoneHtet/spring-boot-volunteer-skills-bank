package group11.volunteer_skills_bank.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "categories")
public class Category {
    @DocumentId
    private String categoryId;
    private String category_name;
    
    // Explicit getters/setters to avoid issues when Lombok annotation processing is unavailable
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }
    
}