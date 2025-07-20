package group11.volunteer_skills_bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;

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

    public Mono<Volunteer> createVolunteer(VolunteerCreationDto dto) 
    {
        if(volunteerCustomRepo.existsByEmail(dto.email())) {
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

    public Mono<Volunteer> updateVolunteer(String id, VolunteerUpdateDto dto) {
        Volunteer volunteer = volunteerRepository.findById(id).block();
        if (volunteer == null) {
            return Mono.empty();
        }
        volunteer.setEmail(dto.email());
        volunteer.setPassword(dto.password());
        volunteer.setFirstName(dto.firstName());
        volunteer.setLastName(dto.lastName());
        volunteer.setAvatarUrl(dto.avatarUrl());
        if (dto.location() != null) {
            volunteer.setLocation(
            new Volunteer.Location(
                                dto.location().city() != null ? dto.location().city() : volunteer.getLocation().getCity(), 
                                dto.location().state() != null ? dto.location().state() : volunteer.getLocation().getState(), 
                                dto.location().coordinates() != null 
                                    ? new GeoPoint(
                                        dto.location().coordinates().latitude(), 
                                        dto.location().coordinates().longitude()) 
                                    : volunteer.getLocation().getCoordinates()));
        }
        volunteer.setBio(dto.bio() != null ? dto.bio() : volunteer.getBio());
        volunteer.setSkills(dto.skills() != null ? dto.skills() : volunteer.getSkills());
        volunteer.setCertifications(dto.certifications() != null ? dto.certifications() : volunteer.getCertifications());
        volunteer.setPrivacySettings(
            dto.privacySettings() != null 
                ? new Volunteer.PrivacySettings(dto.privacySettings().profileVisibility(), dto.privacySettings().searchVisibility()) 
                : volunteer.getPrivacySettings()
        );
        volunteer.setAvailability(dto.availability() != null ? dto.availability() : volunteer.getAvailability());
        // System.out.println(volunteerCustomRepo.getDocumentReference(volunteer.getVolunteerId()));

        return volunteerRepository.save(volunteer); 
    }
    
    public Mono<Void> deleteVolunteer(String id) {
        return volunteerRepository.deleteById(id);
    }
}
