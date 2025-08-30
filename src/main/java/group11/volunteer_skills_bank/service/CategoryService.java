package group11.volunteer_skills_bank.service;

import org.springframework.stereotype.Service;

import group11.volunteer_skills_bank.model.Category;
import group11.volunteer_skills_bank.repository.CategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Mono<Category> create(String categoryName) {
        Category category = new Category();
        category.setCategory_name(categoryName);
        return categoryRepository.save(category)
                .doOnSuccess(c -> System.out.println("CategoryService: Successfully created category: " + c.getCategory_name()))
                .doOnError(e -> System.err.println("CategoryService: Error creating category: " + e.getMessage()));
    }

    public Flux<Category> listAll() {
        return categoryRepository.findAll()
                .doOnNext(c -> System.out.println("CategoryService: Fetched category: " + c.getCategory_name()))
                .doOnError(e -> System.err.println("CategoryService: Error fetching all categories: " + e.getMessage()));
    }

    public Mono<Void> deleteById(String categoryId) {
        return categoryRepository.deleteById(categoryId)
                .doOnSuccess(v -> System.out.println("CategoryService: Successfully deleted categoryId: " + categoryId))
                .doOnError(e -> System.err.println("CategoryService: Error deleting categoryId " + categoryId + ": " + e.getMessage()));
    }
}