package group11.volunteer_skills_bank.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class VolunteerController {
    
    @Autowired
    private VolunteerService volunteerService;

    @PostMapping
    public ResponseEntity<?> createVolunteer(@RequestBody VolunteerCreationDto dto) {
        try {
            Volunteer createdVolunteer = volunteerService.createVolunteer(dto).block();
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVolunteer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error creating volunteer"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<Volunteer>> getVolunteerById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(volunteerService.getVolunteer(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.empty());
        }
    }

    @GetMapping
    public ResponseEntity<Flux<Volunteer>> getAllVolunteers() {
        return ResponseEntity.ok(volunteerService.getAllVolunteers());
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<Mono<Volunteer>> updateVolunteer(
            @PathVariable String id,
            @RequestPart("dto") VolunteerUpdateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "certificateFiles", required = false) List<MultipartFile> certificateFiles
    ) {
        try {
            return ResponseEntity.ok(volunteerService.updateVolunteer(id, dto, image,certificateFiles));
        } catch (IOException | ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body(Mono.empty());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Mono<Void>> deleteVolunteer(@PathVariable String id) {
        try {
            return ResponseEntity.ok(volunteerService.deleteVolunteer(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Mono.empty());
        }
    }

    // New endpoints to support the frontend requirements
    @GetMapping("/{volunteerId}/stats")
    public ResponseEntity<?> getVolunteerStats(@PathVariable String volunteerId) {
        try {
            // Implement your stats logic here
            return ResponseEntity.ok(Map.of(
                    "completedTasks", 10,
                    "ongoingTasks", 2,
                    "skills", List.of("Java", "Spring Boot")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error retrieving stats"));
        }
    }
    
    @PutMapping("/{volunteerId}/status")
    public ResponseEntity<?> updateVolunteerStatus(@PathVariable String volunteerId,@RequestBody Map<String, String> status) {
        try {
            volunteerService.updateStauts(volunteerId, status.get("status"));
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating status"));
        }
    }
}