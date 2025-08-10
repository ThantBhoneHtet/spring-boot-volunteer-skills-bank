package group11.volunteer_skills_bank.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import group11.volunteer_skills_bank.dto.volunteer.VolunteerCreationDto;
import group11.volunteer_skills_bank.dto.volunteer.VolunteerUpdateDto;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.repository.VolunteerRepository;
import group11.volunteer_skills_bank.utils.VolunteerCustomRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class VolunteerService {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private VolunteerCustomRepo volunteerCustomRepo;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private Firestore firestore;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Mono<Volunteer> createVolunteer(VolunteerCreationDto dto) {
        if (volunteerCustomRepo.existsByEmail(dto.email())) {
            return Mono.error(new IllegalArgumentException("Email already exists"));
        }

        Volunteer volunteer = new Volunteer();
        volunteer.setEmail(dto.email());
        volunteer.setFirstName(dto.firstName());
        volunteer.setLastName(dto.lastName());
        volunteer.setPassword(dto.password());
        volunteer.setCompletedOpportunities(0);
        volunteer.setRating(0.0);
        volunteer.setTotalHours(0);
        volunteer.setCreatedAt(Timestamp.now());
        return volunteerRepository.save(volunteer);

    }

    public Mono<Volunteer> getVolunteer(String id) {
        return volunteerRepository.findById(id);
    }

    public Flux<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }

    public Mono<Volunteer> updateVolunteer(String id, VolunteerUpdateDto dto, MultipartFile image)
            throws IOException, ExecutionException, InterruptedException { // Declare exceptions to be thrown

        Volunteer volunteer = volunteerRepository.findById(id).block(); // Block and get the volunteer
        if (volunteer == null) {
            throw new RuntimeException("Volunteer with ID " + id + " not found.");
        }

        // Handle image upload if a new file is provided
        if (image != null) {
            // Optional: Consider deleting the old image from S3 if the URL is changing.
            // This would require fetching the old avatarUrl before setting the new one.
            String newImageUrl = s3Service.uploadFile(image);
            volunteer.setAvatarUrl(newImageUrl); // Set the new URL
        } else if (dto.avatarUrl() != null) {
            volunteer.setAvatarUrl(dto.avatarUrl());
        }

        // Update other fields from DTO if they are not null
        if (dto.password() != null) {
            volunteer.setPassword(dto.password());
        }
        if (dto.firstName() != null) {
            volunteer.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            volunteer.setLastName(dto.lastName());
        }

        if (dto.location() != null) {
            String city = dto.location().city() != null ? dto.location().city()
                    : (volunteer.getLocation() != null ? volunteer.getLocation().getCity() : null);
            String country = dto.location().country() != null ? dto.location().country()
                    : (volunteer.getLocation() != null ? volunteer.getLocation().getCoutry() : null);
            volunteer.setLocation(new Volunteer.Location(city, country));
        }

        if (dto.bio() != null) {
            volunteer.setBio(dto.bio());
        }
        if (dto.skills() != null) {
            volunteer.setSkills(dto.skills());
        }
        if (dto.certifications() != null) {
            volunteer.setCertifications(dto.certifications());
        }
        if(dto.phoneNumber() != null) {
            volunteer.setPhoneNumber(dto.phoneNumber());
        }

        if (dto.privacySettings() != null) {
            volunteer.setPrivacySettings(
                    new Volunteer.PrivacySettings(
                            dto.privacySettings().profileVisibility(),
                            dto.privacySettings().searchVisibility()));
        } else if (volunteer.getPrivacySettings() == null) {
            // If DTO privacy settings are null and existing are null, initialize default
            volunteer.setPrivacySettings(new Volunteer.PrivacySettings(null, null));
        }

        if (dto.availability() != null) {
            volunteer.setAvailability(dto.availability());
        }

        // Update the document in Firestore
        ApiFuture<WriteResult> future = firestore.collection("volunteers").document(id).set(volunteer);
        future.get(); // Wait for the write to complete

        // If you are also using a Spring Data Reactive Repository, you might save it
        // here too synchronously
        return volunteerRepository.save(volunteer); // Block on saving to the repository

    }

    public Mono<Void> deleteVolunteer(String id) {
        return volunteerRepository.deleteById(id);
    }

    public boolean existByEmail(String email) {
        return volunteerCustomRepo.existsByEmail(email);
    }

    public Volunteer login(String email, String password) {
        Volunteer volunteer = volunteerRepository.findByEmail(email).block();
        if (volunteer != null && password.equals(volunteer.getPassword())) {
            return volunteer;
        }
        return null;
    }

    public Volunteer findVolunteerByEmail(String email) {
        return volunteerRepository.findByEmail(email).block();
    }

    public void saveVolunteer(Volunteer volunteer) {
        volunteerRepository.save(volunteer).block();
    }

    public Mono<Volunteer> findVolunteerById(String id) {
        return volunteerRepository.findById(id);
    }

    public void logout(String id) {
        Volunteer volunteer = volunteerRepository.findById(id).block();
        if (volunteer != null) {
            volunteer.setLastLogin(null);
            volunteerRepository.save(volunteer).block();
        }
    }
    
}
