package group11.volunteer_skills_bank.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import group11.volunteer_skills_bank.dto.volunteer.VolunteerCreationDto;
import group11.volunteer_skills_bank.dto.volunteer.VolunteerUpdateDto;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.service.VolunteerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/volunteers")
@CrossOrigin(origins = "*")
public class VolunteerController {
    
    @Autowired
    private VolunteerService volunteerService;

    @PostMapping
    public ResponseEntity<?> createVolunteer(@RequestBody VolunteerCreationDto dto) {
        try{
            return new ResponseEntity<>(volunteerService.createVolunteer(dto).block(), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public Mono<Volunteer> getVolunteerById(@PathVariable String id) {
        return volunteerService.getVolunteer(id);
    }

    @GetMapping
    public Flux<Volunteer> getAllVolunteers() {
        return volunteerService.getAllVolunteers();
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" }) // Assuming 'consumes' is still multipart
    public Mono<Volunteer> updateVolunteer(
            @PathVariable String id,
            @RequestPart("dto") VolunteerUpdateDto dto,
            @RequestPart(value ="image",required=false) MultipartFile image // 'image' is now required as per signature
    ) throws IOException, ExecutionException, InterruptedException {
        return volunteerService.updateVolunteer(id, dto, image);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteVolunteer(@PathVariable String id) {
        return volunteerService.deleteVolunteer(id);
    }
}