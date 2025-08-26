package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.team.TeamRegisterDto;
import group11.volunteer_skills_bank.model.TeamRegister;
import group11.volunteer_skills_bank.model.TeamRegisterTrack;
import group11.volunteer_skills_bank.model.TeamApproval;
import group11.volunteer_skills_bank.model.Volunteer; // Import Volunteer model
import group11.volunteer_skills_bank.repository.TeamApprovalRepository;
import group11.volunteer_skills_bank.repository.TeamRegisterTrackRepository;
import group11.volunteer_skills_bank.service.TeamRegisterService;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.DocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.Timestamp;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/team-registrations")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class TeamRegisterController {

    // ... (rest of the class remains the same)

    private final TeamRegisterService teamRegisterService;
    private final TeamRegisterTrackRepository teamRegisterTrackRepository;
    private final TeamApprovalRepository teamApprovalRepository;
    private final Firestore firestore;

    @Autowired
    public TeamRegisterController(TeamRegisterService teamRegisterService,
                                 TeamRegisterTrackRepository teamRegisterTrackRepository,
                                 TeamApprovalRepository teamApprovalRepository,
                                 Firestore firestore) {
        this.teamRegisterService = teamRegisterService;
        this.teamRegisterTrackRepository = teamRegisterTrackRepository;
        this.teamApprovalRepository = teamApprovalRepository;
        this.firestore = firestore;
    }

    /**
     * Handles the initial registration for a team.
     * This method saves the registration details and also creates a corresponding
     * tracking record in `teamRegisterTrack` to store the volunteer and team IDs.
     *
     * @param dto The registration data transfer object.
     * @return A Mono with a ResponseEntity containing the track ID or an error message.
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> registerForTeam(@RequestBody TeamRegisterDto dto) {
        System.out.println("TeamRegisterController: Received request to register for team " + (dto != null ? dto.teamId() : "null") + " from " + (dto != null ? dto.email() : "null"));
        if (dto == null) {
            System.err.println("TeamRegisterController: DTO is null");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "DTO cannot be null")));
        }

        return teamRegisterService.submitTeamRegistration(dto)
                .flatMap(registration -> {
                    // After successful registration in teamRegister, create or update the track record
                    TeamRegisterTrack track = new TeamRegisterTrack();
                    track.setTrackId(registration.getRegisterId());
                    track.setVolunteerId(registration.getEmail()); // Use email as volunteerId
                    track.setTeamId(registration.getTeamId());
                   // Use the team name from the DTO
                    track.setStatus("Pending");
                  

                    return teamRegisterTrackRepository.saveTeamRegisterTrack(track)
                            .map(savedTrack -> {
                                System.out.println("TeamRegisterController: Successfully saved registration with ID: " + registration.getRegisterId());
                                return ResponseEntity.ok(Map.of("trackId", registration.getRegisterId()));
                            })
                            .onErrorResume(e -> {
                                System.err.println("TeamRegisterController: Error saving track record for ID " + registration.getRegisterId() + ": " + e.getMessage());
                                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error", "Error saving track record")));
                            });
                })
                .onErrorResume(IllegalStateException.class, e -> {
                    System.out.println("TeamRegisterController: Duplicate registration detected: " + e.getMessage());
                    String registerId = e.getMessage().contains("registerId:") ? e.getMessage().split("registerId: ")[1] : "";
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "You have already registered for this team", "trackId", registerId)));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    System.err.println("TeamRegisterController: Invalid input: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Invalid registration data provided")));
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("TeamRegisterController: Error registering for team " + dto.teamId() + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to register for team due to an internal error")));
                });
    }


    /**
     * Retrieves all pending registrations for a specific team.
     * This endpoint now fetches the volunteer's real email.
     *
     * @param teamId The ID of the team.
     * @return A Mono with a ResponseEntity containing a list of pending registrations.
     */
    @GetMapping("/team/{teamId}")
    public Mono<ResponseEntity<?>> getRegistrationsByTeamId(@PathVariable String teamId) {
        System.out.println("TeamRegisterController: Fetching pending registrations for teamId: " + teamId);
        return Mono.fromCallable(() -> {
            if (teamId == null || teamId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Team ID cannot be null or empty"));
            }

            try {
                QuerySnapshot querySnapshot = firestore.collection("teamRegister")
                        .whereEqualTo("teamId", teamId)
                        .get()
                        .get();

                List<Map<String, Object>> registrations = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    TeamRegister reg = doc.toObject(TeamRegister.class);
                    if (reg != null) {
                         // Fetch the status from the teamRegisterTrack collection
                        String status = "Pending"; // Default status
                        try {
                            DocumentSnapshot trackDoc = firestore.collection("teamRegisterTrack")
                                    .document(doc.getId())
                                    .get()
                                    .get();
                            if (trackDoc.exists()) {
                                status = trackDoc.getString("status");
                            }
                        } catch (Exception e) {
                            System.err.println("TeamRegisterController: Error fetching track status for " + doc.getId() + ": " + e.getMessage());
                        }

                        // Only return pending registrations
                        if ("Pending".equals(status)) {
                            // Use the ID from the `email` field to find the real email
                            String volunteerId = reg.getEmail();
                            String volunteerEmail = "Email not found";
                            try {
                                DocumentSnapshot volunteerDoc = firestore.collection("volunteers")
                                        .document(volunteerId)
                                        .get()
                                        .get();
                                if (volunteerDoc.exists()) {
                                    Volunteer volunteer = volunteerDoc.toObject(Volunteer.class);
                                    if (volunteer != null && volunteer.getEmail() != null) {
                                        volunteerEmail = volunteer.getEmail();
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("TeamRegisterController: Error fetching volunteer email for ID " + volunteerId + ": " + e.getMessage());
                            }

                            Map<String, Object> registrationMap = new HashMap<>();
                            registrationMap.put("trackId", doc.getId());
                            registrationMap.put("firstName", reg.getFirstName());
                            registrationMap.put("lastName", reg.getLastName());
                            registrationMap.put("email", volunteerEmail); // Set the real email here
                            registrationMap.put("reason", reg.getReason());
                            registrationMap.put("interests", reg.getInterests());
                            registrationMap.put("teamId", reg.getTeamId());
                            registrationMap.put("createdAt", reg.getCreatedAt());
                            registrationMap.put("status", status);
                            registrationMap.put("volunteernotiId", reg.getEmail());

                            registrations.add(registrationMap);
                        }
                    }
                }
                System.out.println("TeamRegisterController: Found " + registrations.size() + " registrations for teamId: " + teamId);
                return ResponseEntity.ok(registrations);
            } catch (Exception e) {
                System.err.println("TeamRegisterController: Error fetching registrations for teamId " + teamId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to fetch team registrations: " + e.getMessage()));
            }
        });
    }

    /**
     * Retrieves all applications made by a specific volunteer.
     * This endpoint now fetches the team name using the volunteer's unique ID.
     *
     * @param volunteerId The email of the volunteer to look up the unique ID.
     * @return A Mono with a ResponseEntity containing a list of all volunteer applications.
     */
    @GetMapping("/volunteer/{volunteerId}")
    public Mono<ResponseEntity<?>> getRegistrationsByVolunteerId(@PathVariable String volunteerId) {
        System.out.println("TeamRegisterController: Fetching registrations for volunteer email: " + volunteerId);
        return Mono.fromCallable(() -> {
            if (volunteerId == null || volunteerId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Volunteer ID cannot be null or empty"));
            }

            try {
                // Fetch the volunteer's unique ID from their email
                String normalizedEmail = volunteerId.trim().toLowerCase();
                QuerySnapshot volunteerSnapshot = firestore.collection("volunteers")
                        .whereEqualTo("email", normalizedEmail)
                        .limit(1)
                        .get()
                        .get();
                String volunteerUniqueId = null;
                if (!volunteerSnapshot.isEmpty()) {
                    Volunteer volunteer = volunteerSnapshot.getDocuments().get(0).toObject(Volunteer.class);
                    volunteerUniqueId = volunteer.getVolunteerId();
                    System.out.println("TeamRegisterController: Found volunteerId: " + volunteerUniqueId + " for email: " + normalizedEmail);
                } else {
                    System.out.println("TeamRegisterController: No volunteer found for email: " + normalizedEmail);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "No volunteer found for email: " + normalizedEmail));
                }

                QuerySnapshot trackSnapshot = firestore.collection("teamRegisterTrack")
                        .whereEqualTo("volunteerId", volunteerUniqueId)
                        .get()
                        .get();

                List<Map<String, Object>> registrations = new ArrayList<>();
                for (DocumentSnapshot doc : trackSnapshot.getDocuments()) {
                    TeamRegisterTrack track = doc.toObject(TeamRegisterTrack.class);
                    if (track != null) {
                        Map<String, Object> registrationMap = new HashMap<>();
                        registrationMap.put("trackId", track.getTrackId());
                        registrationMap.put("teamId", track.getTeamId());
                        registrationMap.put("status", track.getStatus());
                        registrationMap.put("approvalDate", track.getApprovalDate());

                        // Fetch teamName from the `teams` collection
                        String teamName = "Unknown Team";
                        try {
                            DocumentSnapshot teamDoc = firestore.collection("teams").document(track.getTeamId()).get().get();
                            if (teamDoc.exists()) {
                                teamName = teamDoc.getString("name");
                            }
                        } catch (Exception e) {
                            System.err.println("TeamRegisterController: Error fetching team name for teamId " + track.getTeamId() + ": " + e.getMessage());
                        }
                        registrationMap.put("teamName", teamName);

                        registrations.add(registrationMap);
                    }
                }
                System.out.println("TeamRegisterController: Found " + registrations.size() + " registrations for volunteerId: " + volunteerUniqueId);
                return ResponseEntity.ok(registrations);
            } catch (Exception e) {
                System.err.println("TeamRegisterController: Error fetching registrations for volunteerId " + volunteerId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to fetch volunteer registrations: " + e.getMessage()));
            }
        });
    }

    // ... (rest of the class remains the same)
    
    /**
     * Checks if a volunteer has already registered for a specific team.
     *
     * @param teamId The ID of the team.
     * @param volunteerId The ID of the volunteer.
     * @return A Mono with a ResponseEntity indicating if the volunteer is registered.
     */
    @GetMapping("/check/{teamId}/{volunteerId}")
    public Mono<ResponseEntity<Map<String, Object>>> checkRegistration(@PathVariable String teamId, @PathVariable String volunteerId) {
        System.out.println("TeamRegisterController: Checking registration for teamId: " + teamId + ", volunteerId: " + volunteerId);
        return Mono.fromCallable(() -> {
            try {
                if (teamId == null || teamId.trim().isEmpty() || volunteerId == null || volunteerId.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Invalid teamId or volunteerId"));
                }
                QuerySnapshot querySnapshot = firestore.collection("teamRegister")
                        .whereEqualTo("teamId", teamId)
                        .whereEqualTo("email", volunteerId)
                        .get()
                        .get();
                if (!querySnapshot.isEmpty()) {
                    String registerId = querySnapshot.getDocuments().get(0).getId();
                    System.out.println("TeamRegisterController: Volunteer with volunteerId " + volunteerId + " is registered for team " + teamId + ", registerId: " + registerId);
                    return ResponseEntity.ok(Map.of("isRegistered", true, "trackId", registerId));
                }
                System.out.println("TeamRegisterController: Volunteer with volunteerId " + volunteerId + " is not registered for team " + teamId);
                return ResponseEntity.ok(Map.of("isRegistered", false));
            } catch (Exception e) {
                System.err.println("TeamRegisterController: Error checking registration for teamId " + teamId + ", volunteerId " + volunteerId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to check registration: " + e.getMessage()));
            }
        });
    }

    /**
     * Accepts a volunteer's registration. This adds the volunteer to the team's
     * member list and updates the status in the `teamRegisterTrack` collection.
     *
     * @param registerId The ID of the registration to accept.
     * @return A Mono with a ResponseEntity indicating success or failure.
     */
    @PostMapping("/{registerId}/accept")
    public Mono<ResponseEntity<?>> acceptRegistration(@PathVariable String registerId) {
        System.out.println("TeamRegisterController: Accepting registration " + registerId);
        return Mono.fromCallable(() -> {
            if (registerId == null || registerId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Registration ID cannot be null or empty"));
            }

            try {
                DocumentSnapshot registrationDoc = firestore.collection("teamRegister").document(registerId).get().get();
                if (!registrationDoc.exists()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Registration not found"));
                }

                TeamRegister registration = registrationDoc.toObject(TeamRegister.class);
                if (registration == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to deserialize registration document"));
                }

                // Add the volunteer's email to the team's member list
                firestore.collection("teams").document(registration.getTeamId())
                        .update("members", FieldValue.arrayUnion(registration.getEmail()))
                        .get();
                
                // Update the track record status
                DocumentSnapshot trackDoc = firestore.collection("teamRegisterTrack").document(registerId).get().get();
                if (trackDoc.exists()) {
                    firestore.collection("teamRegisterTrack").document(registerId)
                            .update(
                                "status", "Accepted",
                                "approvalDate", Timestamp.now(),
                                "approvalId", registration.getRegisterId()
                            )
                            .get();
                } else {
                    // Create a new track record if one doesn't exist, this is a fallback.
                    TeamRegisterTrack track = new TeamRegisterTrack();
                    track.setTrackId(registration.getRegisterId());
                    track.setVolunteerId(registration.getEmail());
                    track.setTeamId(registration.getTeamId());
                    track.setStatus("Accepted");
                    track.setApprovalDate(Timestamp.now());
                    track.setApprovalId(registration.getRegisterId());
                    teamRegisterTrackRepository.saveTeamRegisterTrack(track).block();
                }

                System.out.println("TeamRegisterController: Successfully accepted registration " + registerId);
                return ResponseEntity.ok(Map.of("message", "Registration accepted and member added to team"));
            } catch (Exception e) {
                System.err.println("TeamRegisterController: Error accepting registration " + registerId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to accept registration: " + e.getMessage()));
            }
        });
    }

    /**
     * Declines a volunteer's registration. This deletes the pending registration
     * and updates the status in the `teamRegisterTrack` collection.
     *
     * @param registerId The ID of the registration to decline.
     * @return A Mono with a ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{registerId}")
    public Mono<ResponseEntity<?>> declineRegistration(@PathVariable String registerId) {
        System.out.println("TeamRegisterController: Declining registration " + registerId);
        return Mono.fromCallable(() -> {
            if (registerId == null || registerId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Registration ID cannot be null or empty"));
            }

            try {
                // Get the registration details before deleting it
                DocumentSnapshot registrationDoc = firestore.collection("teamRegister").document(registerId).get().get();
                if (!registrationDoc.exists()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Registration not found"));
                }
                
                TeamRegister registration = registrationDoc.toObject(TeamRegister.class);

                // Delete the registration from the 'teamRegister' collection
                firestore.collection("teamRegister").document(registerId).delete().get();

                // Update the track record to "Declined"
                DocumentSnapshot trackDoc = firestore.collection("teamRegisterTrack").document(registerId).get().get();
                if (trackDoc.exists()) {
                    firestore.collection("teamRegisterTrack").document(registerId)
                            .update(
                                "status", "Declined",
                                "approvalDate", Timestamp.now(),
                                "volunteerId", registration.getEmail(),
                                "teamId", registration.getTeamId()
                            )
                            .get();
                } else {
                    // This is a fallback to create the track record if it somehow doesn't exist
                    TeamRegisterTrack track = new TeamRegisterTrack();
                    track.setTrackId(registerId);
                    track.setStatus("Declined");
                    track.setApprovalDate(Timestamp.now());
                    track.setApprovalId(null);
                    track.setVolunteerId(registration.getEmail());
                    track.setTeamId(registration.getTeamId());
                    teamRegisterTrackRepository.saveTeamRegisterTrack(track).block();
                }

                System.out.println("TeamRegisterController: Successfully declined registration " + registerId);
                return ResponseEntity.ok(Map.of("message", "Registration declined successfully"));
            } catch (Exception e) {
                System.err.println("TeamRegisterController: Error declining registration " + registerId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to decline registration: " + e.getMessage()));
            }
        });
    }

}