package group11.volunteer_skills_bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.dto.skill.SkillCreateRequest;
import group11.volunteer_skills_bank.model.Skill;
import group11.volunteer_skills_bank.service.SkillService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "*")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    public ResponseEntity<Mono<Skill>> create(@Valid @RequestBody SkillCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(skillService.create(request.getSkillName(), request.getCategoryId()));
    }

    @GetMapping
    public ResponseEntity<Flux<Skill>> list() {
        return ResponseEntity.ok(skillService.listAll());
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Flux<Skill>> listByCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(skillService.listByCategory(categoryId));
    }
}