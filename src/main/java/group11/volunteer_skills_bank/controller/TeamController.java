package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.dto.team.TeamCreationDto;
import group11.volunteer_skills_bank.dto.team.TeamDetailsResponseDto;
import group11.volunteer_skills_bank.model.Team;
import group11.volunteer_skills_bank.service.TeamService;


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
@CrossOrigin(origins = "*") // Keep this for CORS
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping
    public Mono<ResponseEntity<Team>> createTeam(@RequestBody TeamCreationDto dto) {
        Team team = new Team();
        team.setName(dto.name());
        team.setDescription(dto.description());
        // For creation, DTO comes with emails, service will convert to IDs
        team.setMembers(dto.members());
        team.setLeaders(dto.leaders());
        team.setCreatorId(dto.creatorId()); // This is already an ID

        return teamService.createTeam(team)
                .map(ResponseEntity::ok) // Map the Team object to a 200 OK response
                .onErrorResume(IllegalArgumentException.class, e -> {
                    System.err.println("Controller: Bad request for createTeam: " + e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build()); // 400 Bad Request
                })
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Internal server error for createTeam: " + e.getMessage());
                    // CORRECTED LINE: Create ResponseEntity first, then wrap in Mono.just()
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // 500 Internal Server Error
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
                    // CORRECTED LINE: Create ResponseEntity first, then wrap in Mono.just()
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
                    // CORRECTED LINE: Create ResponseEntity first, then wrap in Mono.just()
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public Mono<ResponseEntity<Team>> removeMember(@PathVariable String teamId, @PathVariable String memberId) {
        return teamService.removeMember(teamId, memberId)
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Controller: Error removing member " + memberId + " from team " + teamId + ": " + e.getMessage());
                    // CORRECTED LINE: Create ResponseEntity first, then wrap in Mono.just()
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
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
                    // CORRECTED LINE: Create ResponseEntity first, then wrap in Mono.just()
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}