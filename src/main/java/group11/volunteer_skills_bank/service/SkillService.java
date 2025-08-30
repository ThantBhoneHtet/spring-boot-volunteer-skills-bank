package group11.volunteer_skills_bank.service;

import org.springframework.stereotype.Service;

import group11.volunteer_skills_bank.model.Skill;
import group11.volunteer_skills_bank.repository.SkillRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public Mono<Skill> create(String skillName, String categoryId) {
        Skill skill = new Skill();
        skill.setSkill_name(skillName);
        skill.setCategoryId(categoryId);
        return skillRepository.save(skill)
                .doOnSuccess(s -> System.out.println("SkillService: Successfully created skill: " + s.getSkill_name()))
                .doOnError(e -> System.err.println("SkillService: Error creating skill: " + e.getMessage()));
    }

    public Flux<Skill> listAll() {
        return skillRepository.findAll()
                .doOnNext(s -> System.out.println("SkillService: Fetched skill: " + s.getSkill_name()))
                .doOnError(e -> System.err.println("SkillService: Error fetching all skills: " + e.getMessage()));
    }

    public Flux<Skill> listByCategory(String categoryId) {
        return skillRepository.findByCategoryId(categoryId)
                .doOnNext(s -> System.out.println("SkillService: Fetched skill for categoryId " + categoryId + ": " + s.getSkill_name()))
                .doOnError(e -> System.err.println("SkillService: Error fetching skills for categoryId " + categoryId + ": " + e.getMessage()));
    }
}