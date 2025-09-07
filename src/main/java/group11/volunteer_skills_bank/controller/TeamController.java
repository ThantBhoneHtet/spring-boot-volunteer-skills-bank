package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.team.TeamCreationDto;
import group11.volunteer_skills_bank.dto.team.TeamDetailsResponseDto;
import group11.volunteer_skills_bank.model.Team;
import group11.volunteer_skills_bank.model.TeamRegisterTrack;
import group11.volunteer_skills_bank.model.Category;
import group11.volunteer_skills_bank.service.TeamService;
import group11.volunteer_skills_bank.repository.TeamRegisterTrackRepository;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private Firestore firestore;

    @Autowired
    private TeamRegisterTrackRepository teamRegisterTrackRepository;

    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createTeam(@RequestBody TeamCreationDto dto) {
        Team team = new Team();
        team.setName(dto.name());
        team.setDescription(dto.description());
        team.setMembers(dto.members());
        team.setLeaders(dto.leaders());
        team.setCreatorId(dto.creatorId());
        team.setCategory(dto.category()); // Save the new category field

        return teamService.createTeam(team)
                .map(savedTeam -> ResponseEntity.ok(Map.of("status", "success", "teamId", savedTeam.getTeamId())))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    System.err.println("Controller: Bad request for createTeam: " + e.getMessage());
                    // Always return the message to the frontend
                    if (e.getMessage().contains("Team name") && e.getMessage().contains("already taken")) {
                        return Mono.just(ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(Map.of("error", e.getMessage())));
                    }
                    return Mono.just(ResponseEntity
                        .badRequest()
                        .body(Map.of("error", e.getMessage())));
                })
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Internal server error for createTeam: " + e.getMessage());
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Unexpected server error. Please try again.")));
                });
    }

    @GetMapping("/categories")
    public Flux<Category> getAllCategories() {
        System.out.println("TeamController: Received request for /categories");
        return teamService.getAllCategories()
            .onErrorResume(RuntimeException.class, e -> {
                System.err.println("Controller: Error fetching all categories: " + e.getMessage());
                return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve categories.", e));
            });
    }

    @GetMapping("/{id}")
    public Mono<TeamDetailsResponseDto> getTeam(@PathVariable String id) {
        return teamService.getTeam(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found with ID: " + id)))
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error fetching team " + id + ": " + e.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve team details.", e));
                });
    }

    @GetMapping
    public Flux<Team> getAllTeams() {
        return teamService.getAllTeams()
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error fetching all teams: " + e.getMessage());
                    return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve all teams.", e));
                });
    }

    @GetMapping("/created-by/{volunteerId}")
    public Flux<Team> getTeamsByCreator(@PathVariable String volunteerId) {
        return teamService.getTeamsByCreator(volunteerId)
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error fetching teams created by " + volunteerId + ": " + e.getMessage());
                    return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve teams created by volunteer.", e));
                });
    }

    @GetMapping("/leader/{volunteerId}")
    public Flux<Team> getTeamsByLeader(@PathVariable String volunteerId) {
        System.out.println("TeamController: Received request for /leader/" + volunteerId);
        return teamService.getTeamsByLeader(volunteerId)
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error fetching teams led by " + volunteerId + ": " + e.getMessage());
                    return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve teams led by volunteer.", e));
                });
    }

    @GetMapping("/member/{volunteerId}")
    public Flux<Team> getTeamsByMember(@PathVariable String volunteerId) {
        System.out.println("TeamController: Received request for /member/" + volunteerId);
        return teamService.getTeamsByMember(volunteerId)
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error fetching teams where " + volunteerId + " is a member: " + e.getMessage());
                    return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve teams where volunteer is a member.", e));
                });
    }

    @PutMapping("/{id}/members")
    public Mono<ResponseEntity<Team>> updateMembers(@PathVariable String id, @RequestBody Map<String, List<String>> body) {
        List<String> newMemberEmails = body.get("members");
        if (newMemberEmails == null) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        return teamService.updateMembers(id, newMemberEmails)
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error updating members for team " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PatchMapping("/{teamId}/members/add")
    public Mono<ResponseEntity<Team>> addMembers(@PathVariable String teamId, @RequestBody List<String> memberEmails) {
        if (memberEmails == null || memberEmails.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        return teamService.addMembers(teamId, memberEmails)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e -> {
                    System.err.println("Controller: Bad request for addMembers: " + e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Internal server error for addMembers: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public Mono<ResponseEntity<Team>> removeMember(@PathVariable String teamId, @PathVariable String memberId) {
        return Mono.fromCallable(() -> {
            try {
                // First, remove the member from the teams collection via the service
                Team updatedTeam = teamService.removeMember(teamId, memberId).block();

                // Find and delete the corresponding teamRegister record
                QuerySnapshot querySnapshot = firestore.collection("teamRegister")
                        .whereEqualTo("teamId", teamId)
                        .whereEqualTo("email", memberId)
                        .get()
                        .get();
                if (!querySnapshot.isEmpty()) {
                    String registerId = querySnapshot.getDocuments().get(0).getId();
                    firestore.collection("teamRegister")
                            .document(registerId)
                            .delete()
                            .get();

                    // Update the teamRegisterTrack status
                    TeamRegisterTrack track = firestore.collection("teamRegisterTrack")
                            .document(registerId)
                            .get()
                            .get()
                            .toObject(TeamRegisterTrack.class);
                    if (track != null) {
                        track.setStatus("Removed");
                        track.setApprovalDate(Timestamp.now());
                        track.setApprovalId(null);
                        teamRegisterTrackRepository.saveTeamRegisterTrack(track).block();
                    } else {
                        track = new TeamRegisterTrack();
                        track.setTrackId(registerId);
                        track.setStatus("Removed");
                        track.setApprovalDate(Timestamp.now());
                        track.setApprovalId(null);
                        teamRegisterTrackRepository.saveTeamRegisterTrack(track).block();
                    }
                }

                return ResponseEntity.ok(updatedTeam);
            } catch (Exception e) {
                System.err.println("Controller: Error removing member " + memberId + " from team " + teamId + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteTeam(@PathVariable String id) {
        return teamService.deleteTeam(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error deleting team " + id + ": " + e.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete team.", e));
                });
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<Team>> updateTeamFields(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return teamService.updateTeamFields(id, updates)
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error updating team fields for team " + id + ": " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<String> getTeamIdByName(@PathVariable String name) {
        try {
            String teamId = teamService.getTeamIdByName(name);
            if (teamId != null) {
                return ResponseEntity.ok(teamId);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No team found with name: " + name);
            }
        } catch (Exception e) {
            System.err.println("Controller: Error fetching team ID by name " + name + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve team ID.");
        }
    }
}