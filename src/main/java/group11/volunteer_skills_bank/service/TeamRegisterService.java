package group11.volunteer_skills_bank.service;

import group11.volunteer_skills_bank.dto.team.TeamRegisterDto;
import group11.volunteer_skills_bank.model.TeamRegister;
import group11.volunteer_skills_bank.model.TeamRegisterTrack;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.repository.TeamRegisterRepo;
import group11.volunteer_skills_bank.repository.TeamRegisterTrackRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TeamRegisterService {

    private final TeamRegisterRepo teamRegisterRepo;
    private final TeamRegisterTrackRepository teamRegisterTrackRepository;
    private final Firestore firestore;

    @Autowired
    public TeamRegisterService(TeamRegisterRepo teamRegisterRepo, TeamRegisterTrackRepository teamRegisterTrackRepository, Firestore firestore) {
        this.teamRegisterRepo = teamRegisterRepo;
        this.teamRegisterTrackRepository = teamRegisterTrackRepository;
        this.firestore = firestore;
    }

    public Mono<TeamRegister> submitTeamRegistration(TeamRegisterDto dto) {
        System.out.println("TeamRegisterService: Processing registration for email: " + dto.email() + ", teamId: " + dto.teamId());
        System.out.println("TeamRegisterService: Full DTO: " + dto);
        return Mono.fromCallable(() -> {
            if (dto == null) {
                throw new IllegalArgumentException("TeamRegisterDto cannot be null.");
            }
            if (dto.teamId() == null || dto.teamId().isEmpty()) {
                throw new IllegalArgumentException("Team ID cannot be null or empty.");
            }
            if (dto.email() == null || dto.email().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty.");
            }
            if (dto.firstName() == null || dto.firstName().isEmpty()) {
                throw new IllegalArgumentException("First name cannot be null or empty.");
            }
            if (dto.lastName() == null || dto.lastName().isEmpty()) {
                throw new IllegalArgumentException("Last name cannot be null or empty.");
            }
            if (dto.reason() == null || dto.reason().isEmpty()) {
                throw new IllegalArgumentException("Reason cannot be null or empty.");
            }
            return dto;
        }).flatMap(dtoValidated -> 
            Mono.fromCallable(() -> fetchVolunteerIdFromEmail(dtoValidated.email()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No volunteer found with email: " + dtoValidated.email())))
                .flatMap(volunteerId -> 
                    teamRegisterRepo.findByTeamIdAndVolunteerId(dtoValidated.teamId(), volunteerId)
                        .flatMap(existing -> Mono.<TeamRegister>error(
                            new IllegalStateException("Volunteer is already registered for this team with registerId: " + existing.getRegisterId())
                        ))
                        .switchIfEmpty(Mono.defer(() -> {
                            TeamRegister registration = new TeamRegister();
                            registration.setRegisterId(null); // Firestore will generate ID
                            registration.setFirstName(dtoValidated.firstName());
                            registration.setLastName(dtoValidated.lastName());
                            registration.setEmail(volunteerId); // Store volunteerId in email field
                            registration.setLocation(dtoValidated.location() != null ? dtoValidated.location() : "");
                            registration.setSkills(dtoValidated.skills()); // Add this line
                            registration.setReason(dtoValidated.reason());
                            registration.setTeamId(dtoValidated.teamId());
                            registration.setCreatedAt(Timestamp.now());
                            System.out.println("TeamRegisterService: Saving registration: " + registration);
                            return teamRegisterRepo.saveTeamRegistration(registration)
                                .flatMap(savedRegistration -> {
                                    if (savedRegistration == null || savedRegistration.getRegisterId() == null) {
                                        return Mono.error(new RuntimeException("Firestore write returned null or no registerId."));
                                    }
                                    TeamRegisterTrack track = new TeamRegisterTrack();
                                    track.setTrackId(savedRegistration.getRegisterId());
                                    track.setVolunteerId(volunteerId); // Set the fetched volunteerId
                                    track.setTeamId(dtoValidated.teamId()); // Set the teamId
                                    track.setStatus("Pending");
                                    track.setApprovalDate(Timestamp.now());
                                    track.setApprovalId(null);
                                    System.out.println("TeamRegisterService: Creating TeamRegisterTrack with ID: " + savedRegistration.getRegisterId() + ", volunteerId: " + volunteerId + ", teamId: " + dtoValidated.teamId() + ", status: Pending");
                                    return teamRegisterTrackRepository.saveTeamRegisterTrack(track)
                                        .thenReturn(savedRegistration);
                                });
                        }))
                )
        )
        .doOnSuccess(registration -> System.out.println("TeamRegisterService: Successfully saved registration with ID: " + registration.getRegisterId()))
        .doOnError(e -> {
            System.err.println("TeamRegisterService: Error saving registration: " + e.getMessage());
            e.printStackTrace();
        })
        .onErrorResume(e -> Mono.error(new RuntimeException("Failed to submit team registration: " + e.getMessage(), e)));
    }

    private String fetchVolunteerIdFromEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection("volunteers")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get();
            if (!querySnapshot.isEmpty()) {
                Volunteer volunteer = querySnapshot.getDocuments().get(0).toObject(Volunteer.class);
                System.out.println("TeamRegisterService: Found volunteerId: " + volunteer.getVolunteerId() + " for email: " + email);
                return volunteer.getVolunteerId();
            }
            System.out.println("TeamRegisterService: No volunteer found for email: " + email);
            return null;
        } catch (Exception e) {
            System.err.println("TeamRegisterService: Error fetching volunteerId for email " + email + ": " + e.getMessage());
            return null;
        }
    }
}