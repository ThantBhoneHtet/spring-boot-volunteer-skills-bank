package group11.volunteer_skills_bank.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import group11.volunteer_skills_bank.dto.feedback.VolunteerFeedbackResponseDto;
import group11.volunteer_skills_bank.dto.volunteer.VolunteerCreationDto;
import group11.volunteer_skills_bank.dto.volunteer.VolunteerUpdateDto;
import group11.volunteer_skills_bank.model.Feedback;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.service.FeedbackService;
import group11.volunteer_skills_bank.service.OrganizationService;
import group11.volunteer_skills_bank.service.VolunteerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/volunteers")
@CrossOrigin(origins = "*")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private OrganizationService organizationService;

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
            @RequestPart(value = "certificateFiles", required = false) List<MultipartFile> certificateFiles) {
        try {
            return ResponseEntity.ok(volunteerService.updateVolunteer(id, dto, image, certificateFiles));
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

    @GetMapping("/{volunteerId}/stats")
    public ResponseEntity<?> getVolunteerStats(@PathVariable String volunteerId) {
        try {
            return ResponseEntity.ok(Map.of(
                    "completedTasks", 10,
                    "ongoingTasks", 2,
                    "skills", List.of("Java", "Spring Boot")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error retrieving stats"));
        }
    }

    @PutMapping("/{volunteerId}/status")
    public ResponseEntity<?> updateVolunteerStatus(@PathVariable String volunteerId,
            @RequestBody Map<String, String> status) {
        try {
            volunteerService.updateStauts(volunteerId, status.get("status"));
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating status"));
        }
    }

    @GetMapping("/top-rated-volunteers")
    public ResponseEntity<?> getTopRatedVolunteers(@RequestBody(required = false) Map<String, Object> filters) {
        List<Feedback> feedbacks;

        try {
            feedbacks = feedbackService.getTopRatedVolunteers(filters);
            System.out.println("Fetched feedbacks: " + (feedbacks != null ? feedbacks.size() : "null"));

            List<VolunteerFeedbackResponseDto> topRatingVolunteersDtos = feedbacks.stream()
                    .map(feedback -> {
                        if (feedback == null) {
                            System.out.println("Null feedback found!");
                            return null;
                        }

                        System.out.println("Processing feedbackId: " + feedback.getFeedbackId());

                        Volunteer volunteer = volunteerService.getVolunteer(feedback.getFeedbackId())
                                .blockOptional()
                                .orElse(null);
                        if (volunteer == null) {
                            System.out.println("Volunteer not found for feedbackId: " + feedback.getFeedbackId());
                            return null;
                        }

                        List<VolunteerFeedbackResponseDto.FeedbackDetail> feedbackDetails = feedbackService
                                .mapFeedbackDetails(feedback);

                        return new VolunteerFeedbackResponseDto(
                                volunteer.getVolunteerId(),
                                volunteer.getFirstName(),
                                volunteer.getLastName(),
                                volunteer.getAvatarUrl(),
                                volunteer.getTotalHours(),
                                feedback.getWeightedAverageScore(),
                                feedbackDetails);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            System.out.println("Successfully built TopRatingVolunteersDto list: " + topRatingVolunteersDtos.size());

            return ResponseEntity.ok(topRatingVolunteersDtos);
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching top-rated volunteers", e);
        }
    }

    @GetMapping("/{volunteerId}/feedbacks")
    public ResponseEntity<?> getFeedbacksForVolunteer(@PathVariable String volunteerId) {
        Volunteer volunteer = volunteerService.getVolunteer(volunteerId).block();
        if (volunteer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Volunteer not found with id: " + volunteerId));
        }

        Feedback feedbacks = feedbackService.getFeedbacksForVolunteer(volunteerId);
        System.out.println("Fetched feedbacks for volunteerId " + volunteerId + ": "
                + (feedbacks != null ? feedbacks.getFeedbackDetialsList().size() : "null"));
        if (feedbacks != null) {
            List<VolunteerFeedbackResponseDto.FeedbackDetail> feedbackDetails = feedbackService
                    .mapFeedbackDetails(feedbacks);
            return ResponseEntity.ok(
                    new VolunteerFeedbackResponseDto(
                            volunteer.getVolunteerId(),
                            volunteer.getFirstName(),
                            volunteer.getLastName(),
                            volunteer.getAvatarUrl(),
                            volunteer.getTotalHours(),
                            feedbacks.getWeightedAverageScore(),
                            feedbackDetails));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "No feedbacks found for volunteerId: " + volunteerId));
        }

    }

    @GetMapping("/count/active")
    public Mono<Long> getTotalActiveVolunteers() {
        return volunteerService.getTotalActiveVolunteers();
    }

}