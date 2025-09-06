package group11.volunteer_skills_bank.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import group11.volunteer_skills_bank.dto.team.TeamDetailsResponseDto;
import group11.volunteer_skills_bank.model.Category;
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
     * Checks if a team name already exists in the database.
     *
     * @param teamName The name of the team to check.
     * @return A Mono emitting true if the team name exists, false otherwise.
     */
    private Mono<Boolean> teamNameExists(String teamName) {
        System.out.println("TeamService: Checking if team name exists: " + teamName);
        return Mono.fromCallable(() -> {
            QuerySnapshot querySnapshot = firestore.collection("teams")
                    .whereEqualTo("name", teamName)
                    .get()
                    .get();
            boolean exists = !querySnapshot.isEmpty();
            System.out.println("TeamService: Team name '" + teamName + "' exists: " + exists);
            return exists;
        }).doOnError(e -> System.err.println("TeamService: Error checking team name existence: " + e.getMessage()));
    }

    /**
     * Converts a list of email addresses to a list of Volunteer IDs.
     * If any email does not correspond to an existing volunteer, an IllegalArgumentException is thrown.
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
                        .switchIfEmpty(Mono.defer(() -> {
                            System.err.println("TeamService: Error: Volunteer with email '" + email + "' not found.");
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

        // Check if team name already exists
        return teamNameExists(team.getName())
                .flatMap(exists -> {
                    if (exists) {
                        System.err.println("TeamService: Team name '" + team.getName() + "' already exists.");
                        return Mono.error(new IllegalArgumentException("Team name '" + team.getName() + "' is already taken. Please choose a different name."));
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
                            );
                });
    }

    /**
     * Fetches all categories from the database.
     * @return a Flux of all Category documents.
     */
    public Flux<Category> getAllCategories() {
        System.out.println("TeamService: getAllCategories called.");
        return Flux.create(sink -> {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("categories").get();
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                for (QueryDocumentSnapshot document : documents) {
                    Category category = document.toObject(Category.class);
                    category.setCategoryId(document.getId());
                    sink.next(category);
                }
                sink.complete();
            } catch (InterruptedException | ExecutionException e) {
                sink.error(new RuntimeException("Error fetching categories", e));
            }
        });
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
                .doOnError(e -> System.err.println("TeamService: Error updating members for teamId " + teamId + ": " + e.getMessage()));
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
        .doOnError(e -> System.err.println("TeamService: Error adding members to team " + teamId + ": " + e.getMessage()));
    }

    public Mono<Team> removeMember(String teamId, String memberIdToRemove) {
        System.out.println("TeamService: removeMember called for teamId: " + teamId + ", member ID to remove: " + memberIdToRemove);
        return Mono.fromCallable(() -> {
            DocumentReference teamRef = firestore.collection("teams").document(teamId);
            ApiFuture<WriteResult> future = teamRef.update("members", FieldValue.arrayRemove(memberIdToRemove));
            future.get();
            System.out.println("TeamService: Member ID '" + memberIdToRemove + "' removed successfully from teamId: " + teamId);
            return getTeamByIdInternal(teamId);
        }).doOnError(e -> System.err.println("TeamService: Error removing member " + memberIdToRemove + " from teamId " + teamId + ": " + e.getMessage()));
    }

    public Mono<Void> deleteTeam(String teamId) {
        System.out.println("TeamService: deleteTeam called for teamId: " + teamId);
        return Mono.fromCallable(() -> {
            firestore.collection("teams").document(teamId).delete().get();
            System.out.println("TeamService: Team " + teamId + " deleted successfully.");
            return null;
        }).then();
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
                .doOnError(e -> System.err.println("TeamService: Error updating team fields for teamId " + teamId + ": " + e.getMessage()));
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
    
    /**
     * Finds a team by its name.
     *
     * @param teamName The name of the team to find.
     * @return A Mono containing the team if found, empty otherwise.
     */
    public Mono<Team> findTeamByName(String teamName) {
        System.out.println("TeamService: findTeamByName called for teamName: " + teamName);
        return Mono.fromCallable(() -> {
            QuerySnapshot querySnapshot = firestore.collection("teams")
                    .whereEqualTo("name", teamName)
                    .get().get();
            
            if (querySnapshot.isEmpty()) {
                System.out.println("TeamService: No team found with name: " + teamName);
                return null;
            }
            
            DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
            Team team = snapshot.toObject(Team.class);
            team.setTeamId(snapshot.getId());
            System.out.println("TeamService: Found team: " + team.getName() + " (ID: " + team.getTeamId() + ")");
            return team;
        })
        .doOnError(e -> System.err.println("TeamService: Error finding team by name " + teamName + ": " + e.getMessage()))
        .onErrorResume(e -> Mono.error(new RuntimeException("Error finding team by name: " + e.getMessage(), e)));
    }

    /**
     * Validates if a volunteer is a leader of a specific team by team name.
     * Returns specific validation results to distinguish between different error scenarios.
     *
     * @param volunteerId The volunteer ID to check.
     * @param teamName The name of the team to validate leadership for.
     * @return A Mono<String> with validation result: "valid", "team_not_found", or "not_leader"
     */
    public Mono<String> validateVolunteerTeamLeadership(String volunteerId, String teamName) {
        System.out.println("TeamService: validateVolunteerTeamLeadership called for volunteerId: " + volunteerId + ", teamName: " + teamName);
        return findTeamByName(teamName)
                .map(team -> {
                    if (team == null) {
                        System.out.println("TeamService: Team not found: " + teamName);
                        return "team_not_found";
                    }
                    boolean isLeader = team.getLeaders() != null && team.getLeaders().contains(volunteerId);
                    System.out.println("TeamService: Volunteer " + volunteerId + " is leader of team " + teamName + ": " + isLeader);
                    return isLeader ? "valid" : "not_leader";
                })
                .switchIfEmpty(Mono.just("team_not_found"))
                .doOnError(e -> System.err.println("TeamService: Error validating leadership for volunteer " + volunteerId + " in team " + teamName + ": " + e.getMessage()))
                .onErrorReturn("error");
    }

    /**
     * Validates if a volunteer is a leader of a specific team by team name.
     *
     * @param volunteerId The volunteer ID to check.
     * @param teamName The name of the team to validate leadership for.
     * @return A Mono<Boolean> indicating if the volunteer is a leader of the specified team.
     */
    public Mono<Boolean> isVolunteerLeaderOfTeam(String volunteerId, String teamName) {
        return validateVolunteerTeamLeadership(volunteerId, teamName)
                .map(result -> "valid".equals(result));
    }
    
    
}