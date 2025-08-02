package group11.volunteer_skills_bank.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import group11.volunteer_skills_bank.dto.team.TeamDetailsResponseDto;
import group11.volunteer_skills_bank.model.Team;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.utils.TeamCustomRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TeamCustomRepo teamCustomRepo;

    /**
     * Converts a list of email addresses to a list of Volunteer IDs.
     * If any email does not correspond to an existing volunteer, an IllegalArgumentException is thrown.
     * This ensures that only valid, existing volunteer emails are processed for team members/leaders.
     *
     * @param emails The list of email addresses to convert.
     * @return A Mono emitting a list of Volunteer IDs.
     */
    private Mono<List<String>> convertEmailsToVolunteerIds(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Mono.just(new ArrayList<>());
        }
        System.out.println("TeamService: Attempting to convert emails to volunteer IDs: " + emails);

        return Flux.fromIterable(emails)
                .filter(email -> email != null && !email.isBlank())
                .flatMap(email -> teamCustomRepo.getVolunteerByEmail(email.trim())
                        .map(Volunteer::getVolunteerId)
                        .doOnNext(id -> System.out.println("TeamService: Converted email '" + email + "' to ID: " + id))
                        // Changed to throw an error if a volunteer email is not found
                        .switchIfEmpty(Mono.defer(() -> {
                            System.err.println("TeamService: Error: Volunteer with email '" + email + "' not found. This email cannot be added to the team.");
                            return Mono.error(new IllegalArgumentException("Volunteer with email '" + email + "' not found."));
                        }))
                )
                .collectList()
                .doOnError(e -> System.err.println("TeamService: Error during email to ID conversion: " + e.getMessage()));
    }


    public Mono<Team> createTeam(Team team) {
        System.out.println("TeamService: createTeam called for team: " + (team != null ? team.getName() : "null"));

        if (team.getCreatorId() == null || team.getCreatorId().isEmpty()) {
            return Mono.error(new IllegalArgumentException("creatorId is required to create a team."));
        }

        Mono<List<String>> leaderIdsMono = convertEmailsToVolunteerIds(team.getLeaders());
        Mono<List<String>> memberIdsMono = convertEmailsToVolunteerIds(team.getMembers());

        return teamCustomRepo.getVolunteerById(team.getCreatorId())
                .switchIfEmpty(Mono.error(new RuntimeException("Creator volunteer not found with ID: " + team.getCreatorId())))
                .flatMap(creatorVolunteer -> Mono.zip(leaderIdsMono, memberIdsMono)
                    .flatMap(tuple -> {
                        List<String> leadersFromEmails = tuple.getT1();
                        List<String> membersFromEmails = tuple.getT2();

                        Set<String> leaderIdSet = new HashSet<>(leadersFromEmails);
                        List<String> distinctMembers = membersFromEmails.stream()
                                .filter(memberId -> !leaderIdSet.contains(memberId))
                                .distinct()
                                .collect(Collectors.toList());

                        team.setLeaders(leadersFromEmails.stream().distinct().collect(Collectors.toList()));
                        team.setMembers(distinctMembers);

                        return Mono.fromCallable(() -> {
                            DocumentReference docRef = firestore.collection("teams").document();
                            team.setTeamId(docRef.getId());
                            team.setCreatedAt(Timestamp.now());

                            System.out.println("TeamService: Saving new team to Firestore: " + team.getName() + " (ID: " + team.getTeamId() + ")");
                            System.out.println("TeamService: Final Members (IDs): " + team.getMembers() + ", Final Leaders (IDs): " + team.getLeaders());
                            docRef.set(team).get();
                            System.out.println("TeamService: Team saved successfully.");
                            return team;
                        });
                    })
                )
                .doOnError(e -> System.err.println("TeamService: Error creating team (reactive chain): " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error creating team: " + e.getMessage(), e)));
    }


    public Mono<TeamDetailsResponseDto> getTeam(String teamId) {
        System.out.println("TeamService: getTeam called for teamId: " + teamId);
        return Mono.fromCallable(() -> {
            DocumentSnapshot snapshot = firestore.collection("teams").document(teamId).get().get();
            if (!snapshot.exists()) {
                System.err.println("TeamService: getTeam failed: Team not found for ID: " + teamId);
                throw new RuntimeException("Team not found with ID: " + teamId);
            }
            Team team = snapshot.toObject(Team.class);
            team.setTeamId(snapshot.getId());
            System.out.println("TeamService: Found team: " + team.getName() + " (ID: " + team.getTeamId() + "). Stored members (IDs): " + team.getMembers() + ", Leaders (IDs): " + team.getLeaders());

            List<Volunteer> fullMembers = teamCustomRepo.getTeamMembers(team.getTeamId());
            List<Volunteer> fullLeaders = teamCustomRepo.getTeamLeaders(team.getTeamId());
            System.out.println("TeamService: Hydrated members count: " + fullMembers.size() + ", leaders count: " + fullLeaders.size());

            TeamDetailsResponseDto dto = new TeamDetailsResponseDto();
            dto.setTeamId(team.getTeamId());
            dto.setName(team.getName());
            dto.setDescription(team.getDescription());
            dto.setMembers(fullMembers);
            dto.setLeaders(fullLeaders);
            dto.setCreatorId(team.getCreatorId());
            dto.setCreatedAt(team.getCreatedAt());
       
            return dto;
        }).onErrorResume(e -> {
            System.err.println("TeamService: Error fetching team details for ID " + teamId + ": " + e.getMessage());
            return Mono.error(new RuntimeException("Error fetching team details: " + e.getMessage(), e));
        });
    }

    public Flux<Team> getAllTeams() {
        System.out.println("TeamService: getAllTeams called (fetching all teams).");
        return Flux.fromIterable(
                        firestore.collection("teams").listDocuments()
                ).flatMap(docRef -> Mono.fromCallable(() -> {
                    DocumentSnapshot snapshot = docRef.get().get();
                    Team team = snapshot.toObject(Team.class);
                    if (team != null) {
                        team.setTeamId(snapshot.getId());
                        System.out.println("TeamService: Emitting all team: " + team.getName() + " (ID: " + team.getTeamId() + ")");
                    }
                    return team;
                }).onErrorResume(e -> {
                    System.err.println("TeamService: Error fetching team document " + docRef.getId() + ": " + e.getMessage());
                    return Mono.empty();
                }))
                .doOnError(e -> System.err.println("TeamService: Error in getAllTeams Flux: " + e.getMessage()));
    }

    public Flux<Team> getTeamsByCreator(String volunteerId) {
        System.out.println("TeamService: getTeamsByCreator called for volunteerId: " + volunteerId);
        return teamCustomRepo.findByCreatorId(volunteerId)
                .doOnError(e -> System.err.println("TeamService: Error in getTeamsByCreator: " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching teams created by volunteer: " + e.getMessage(), e)));
    }

    public Flux<Team> getTeamsByLeader(String volunteerId) {
        System.out.println("TeamService: getTeamsByLeader called for volunteerId: " + volunteerId);
        return teamCustomRepo.findByLeadersContaining(volunteerId)
                .doOnError(e -> System.err.println("TeamService: Error in getTeamsByLeader: " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching teams led by volunteer: " + e.getMessage(), e)));
    }

    public Flux<Team> getTeamsByMember(String volunteerId) {
        System.out.println("TeamService: getTeamsByMember called for volunteerId: " + volunteerId);
        return teamCustomRepo.findByMembersContaining(volunteerId)
                .doOnError(e -> System.err.println("TeamService: Error in getTeamsByMember: " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching teams member of volunteer: " + e.getMessage(), e)));
    }


    public Mono<Team> updateMembers(String teamId, List<String> newMemberEmails) {
        System.out.println("TeamService: updateMembers called for teamId: " + teamId + " with new emails: " + newMemberEmails);
        return convertEmailsToVolunteerIds(newMemberEmails)
                .flatMap(newMemberIds -> Mono.fromCallable(() -> {
                    DocumentReference teamRef = firestore.collection("teams").document(teamId);
                    List<String> cleanedIds = newMemberIds.stream()
                                                            .filter(id -> id != null && !id.isBlank())
                                                            .distinct()
                                                            .collect(Collectors.toList());
                    System.out.println("TeamService: Updating members to IDs: " + cleanedIds);
                    ApiFuture<WriteResult> future = teamRef.update("members", cleanedIds);
                    future.get();
                    System.out.println("TeamService: Members updated successfully for teamId: " + teamId);
                    return getTeamByIdInternal(teamId);
                }))
                .doOnError(e -> System.err.println("TeamService: Error updating members for teamId " + teamId + ": " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error updating members: " + e.getMessage(), e)));
    }

    public Mono<Team> addMembers(String teamId, List<String> memberEmailsToAdd) {
        System.out.println("TeamService: addMembers called for teamId: " + teamId + " with new emails: " + memberEmailsToAdd);
        return Mono.fromCallable(() -> {
            DocumentReference teamRef = firestore.collection("teams").document(teamId);
            DocumentSnapshot teamSnapshot = teamRef.get().get();

            if (!teamSnapshot.exists()) {
                System.err.println("TeamService: addMembers failed: Team not found with ID: " + teamId);
                throw new RuntimeException("Team not found with ID: " + teamId);
            }
            return teamSnapshot.toObject(Team.class);
        })
        .flatMap(team -> convertEmailsToVolunteerIds(memberEmailsToAdd)
            .flatMap(newMemberIds -> Mono.fromCallable(() -> {
                List<String> currentMemberIds = team.getMembers() != null ? new ArrayList<>(team.getMembers()) : new ArrayList<>();
                List<String> idsToAdd = newMemberIds.stream()
                        .filter(id -> id != null && !id.isBlank() && !currentMemberIds.contains(id))
                        .collect(Collectors.toList());

                if (!idsToAdd.isEmpty()) {
                    System.out.println("TeamService: Adding member IDs to team " + teamId + ": " + idsToAdd);
                    DocumentReference teamRef = firestore.collection("teams").document(teamId);
                    ApiFuture<WriteResult> future = teamRef.update("members", FieldValue.arrayUnion(idsToAdd.toArray()));
                    future.get();
                    System.out.println("TeamService: Members added successfully for teamId: " + teamId);
                } else {
                    System.out.println("TeamService: No new valid members to add for teamId: " + teamId);
                }
                return getTeamByIdInternal(teamId);
            }))
        )
        .doOnError(e -> System.err.println("TeamService: Error adding members to team " + teamId + ": " + e.getMessage()))
        .onErrorResume(e -> Mono.error(new RuntimeException("Error adding members to team: " + e.getMessage(), e)));
    }


    public Mono<Team> removeMember(String teamId, String memberIdToRemove) {
        System.out.println("TeamService: removeMember called for teamId: " + teamId + ", member ID to remove: " + memberIdToRemove);
        return Mono.fromCallable(() -> {
            DocumentReference teamRef = firestore.collection("teams").document(teamId);
            ApiFuture<WriteResult> future = teamRef.update("members", FieldValue.arrayRemove(memberIdToRemove));
            future.get();
            System.out.println("TeamService: Member ID '" + memberIdToRemove + "' removed successfully from teamId: " + teamId);
            return getTeamByIdInternal(teamId);
        }).doOnError(e -> System.err.println("TeamService: Error removing member " + memberIdToRemove + " from teamId " + teamId + ": " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error removing member: " + e.getMessage(), e)));
    }

    public Mono<Void> deleteTeam(String teamId) {
        System.out.println("TeamService: deleteTeam called for teamId: " + teamId);
        return Mono.fromCallable(() -> {
            firestore.collection("teams").document(teamId).delete().get();
            System.out.println("TeamService: Team " + teamId + " deleted successfully.");
            return null;
        }).then()
          .doOnError(e -> System.err.println("TeamService: Error in deleteTeam reactive chain: " + e.getMessage()))
          .onErrorResume(e -> Mono.error(new RuntimeException("Error deleting team: " + e.getMessage(), e)));
    }


    public Mono<Team> updateTeamFields(String teamId, Map<String, Object> updates) {
        System.out.println("TeamService: updateTeamFields called for teamId: " + teamId + " with updates: " + updates);

        Mono<List<String>> membersConversionMono = Mono.just(new ArrayList<>());
        if (updates.containsKey("members") && updates.get("members") instanceof List) {
            List<String> emails = (List<String>) updates.get("members");
            membersConversionMono = convertEmailsToVolunteerIds(emails);
            System.out.println("TeamService: Members list found in updates, preparing for conversion.");
        }

        Mono<List<String>> leadersConversionMono = Mono.just(new ArrayList<>());
        if (updates.containsKey("leaders") && updates.get("leaders") instanceof List) {
            List<String> emails = (List<String>) updates.get("leaders");
            leadersConversionMono = convertEmailsToVolunteerIds(emails);
            System.out.println("TeamService: Leaders list found in updates, preparing for conversion.");
        }

        return Mono.zip(membersConversionMono, leadersConversionMono)
                .flatMap(tuple -> Mono.fromCallable(() -> {
                    DocumentReference teamRef = firestore.collection("teams").document(teamId);
                    updates.remove("createdAt");
                    updates.remove("teamId");

                    if (updates.containsKey("members") && updates.get("members") instanceof List) {
                        updates.put("members", tuple.getT1().stream().distinct().filter(id -> id != null && !id.isBlank()).collect(Collectors.toList()));
                        System.out.println("TeamService: Converted and cleaned members in updates: " + updates.get("members"));
                    }
                    if (updates.containsKey("leaders") && updates.get("leaders") instanceof List) {
                        updates.put("leaders", tuple.getT2().stream().distinct().filter(id -> id != null && !id.isBlank()).collect(Collectors.toList()));
                        System.out.println("TeamService: Converted and cleaned leaders in updates: " + updates.get("leaders"));
                    }

                    updates.put("updatedAt", Timestamp.now());
                    System.out.println("TeamService: Applying updates to team " + teamId + ": " + updates);
                    ApiFuture<WriteResult> future = teamRef.update(updates);
                    future.get();
                    System.out.println("TeamService: Team fields updated successfully for teamId: " + teamId);
                    return getTeamByIdInternal(teamId);
                }))
                .doOnError(e -> System.err.println("TeamService: Error updating team fields for teamId " + teamId + ": " + e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error updating team fields: " + e.getMessage(), e)));
    }


    private Team getTeamByIdInternal(String teamId) throws ExecutionException, InterruptedException {
        System.out.println("TeamService: getTeamByIdInternal called for teamId: " + teamId);
        DocumentSnapshot snapshot = firestore.collection("teams").document(teamId).get().get();
        if (!snapshot.exists()) {
            System.err.println("TeamService: getTeamByIdInternal failed: Team not found with ID: " + teamId);
            throw new RuntimeException("Team not found with ID: " + teamId);
        }
        Team team = snapshot.toObject(Team.class);
        team.setTeamId(snapshot.getId());
        System.out.println("TeamService: Retrieved team internally: " + team.getName() + " (ID: " + team.getTeamId() + ")");
        return team;
    }
}