package group11.volunteer_skills_bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.dto.category.CategoryCreateRequest;
import group11.volunteer_skills_bank.model.Category;
import group11.volunteer_skills_bank.service.CategoryService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public ResponseEntity<Mono<Category>> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(request.getCategoryName()));
    }

    @GetMapping("/categories")
    public ResponseEntity<Flux<Category>> list() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Mono<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.deleteById(id));
    }
}