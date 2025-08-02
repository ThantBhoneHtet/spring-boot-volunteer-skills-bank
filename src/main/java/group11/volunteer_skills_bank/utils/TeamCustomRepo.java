package group11.volunteer_skills_bank.utils;

import com.google.cloud.firestore.*;
import group11.volunteer_skills_bank.model.Team;
import group11.volunteer_skills_bank.model.Volunteer;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
// import java.util.stream.Collectors; // Not used in this version, but can keep if needed elsewhere

@Repository
public class TeamCustomRepo {

    private final Firestore firestore;

    public TeamCustomRepo(Firestore firestore) {
        this.firestore = firestore;
    }

    private static final String TEAMS_COLLECTION = "teams";
    private static final String VOLUNTEERS_COLLECTION = "volunteers";

    public Flux<Team> findByCreatorId(String volunteerId) {
        System.out.println("TeamCustomRepo: findByCreatorId called for volunteerId: " + volunteerId);
        return Flux.create(sink -> {
            try {
                QuerySnapshot querySnapshot = firestore.collection(TEAMS_COLLECTION)
                        .whereEqualTo("creatorId", volunteerId)
                        .get()
                        .get();

                if (querySnapshot.isEmpty()) {
                    System.out.println("TeamCustomRepo: No teams found created by: " + volunteerId);
                } else {
                    System.out.println("TeamCustomRepo: Found " + querySnapshot.size() + " teams created by: " + volunteerId);
                }

                for (QueryDocumentSnapshot document : querySnapshot) {
                    Team team = document.toObject(Team.class);
                    team.setTeamId(document.getId());
                    System.out.println("TeamCustomRepo: Emitting created team: " + team.getName() + " (ID: " + team.getTeamId() + ")");
                    sink.next(team);
                }
                sink.complete();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("TeamCustomRepo: Error querying teams by creatorId: " + e.getMessage());
                sink.error(new RuntimeException("Error querying teams by creatorId: " + e.getMessage(), e));
            }
        });
    }

    /**
     * Finds teams where the given volunteerId is present in the 'leaders' array.
     * Assumes 'leaders' array stores Volunteer IDs (Firestore document IDs for Volunteer).
     */
    public Flux<Team> findByLeadersContaining(String leaderVolunteerId) {
        System.out.println("TeamCustomRepo: findByLeadersContaining called for leader Volunteer ID: " + leaderVolunteerId);
        return Flux.create(sink -> {
            try {
                QuerySnapshot querySnapshot = firestore.collection(TEAMS_COLLECTION)
                        .whereArrayContains("leaders", leaderVolunteerId)
                        .get()
                        .get();

                if (querySnapshot.isEmpty()) {
                    System.out.println("TeamCustomRepo: No teams found where '" + leaderVolunteerId + "' is a leader.");
                } else {
                    System.out.println("TeamCustomRepo: Found " + querySnapshot.size() + " teams for leader ID: " + leaderVolunteerId);
                }

                for (QueryDocumentSnapshot document : querySnapshot) {
                    Team team = document.toObject(Team.class);
                    team.setTeamId(document.getId());
                    System.out.println("TeamCustomRepo: Emitting team (leader): " + team.getName() + " (ID: " + team.getTeamId() + ")");
                    sink.next(team);
                }
                sink.complete();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("TeamCustomRepo: Error querying teams by leader ID: " + e.getMessage());
                sink.error(new RuntimeException("Error querying teams by leader ID: " + e.getMessage(), e));
            }
        });
    }

    /**
     * Finds teams where the given volunteerId is present in the 'members' array.
     * Assumes 'members' array stores Volunteer IDs (Firestore document IDs for Volunteer).
     */
    public Flux<Team> findByMembersContaining(String memberVolunteerId) {
        System.out.println("TeamCustomRepo: findByMembersContaining called for member Volunteer ID: " + memberVolunteerId);
        return Flux.create(sink -> {
            try {
                QuerySnapshot querySnapshot = firestore.collection(TEAMS_COLLECTION)
                        .whereArrayContains("members", memberVolunteerId)
                        .get()
                        .get();

                if (querySnapshot.isEmpty()) {
                    System.out.println("TeamCustomRepo: No teams found where '" + memberVolunteerId + "' is a member.");
                } else {
                    System.out.println("TeamCustomRepo: Found " + querySnapshot.size() + " teams for member ID: " + memberVolunteerId);
                }

                for (QueryDocumentSnapshot document : querySnapshot) {
                    Team team = document.toObject(Team.class);
                    team.setTeamId(document.getId());
                    System.out.println("TeamCustomRepo: Emitting team (member): " + team.getName() + " (ID: " + team.getTeamId() + ")");
                    sink.next(team);
                }
                sink.complete();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("TeamCustomRepo: Error querying teams by member ID: " + e.getMessage());
                sink.error(new RuntimeException("Error querying teams by member ID: " + e.getMessage(), e));
            }
        });
    }

    /**
     * Retrieves full Volunteer objects for all members of a given team.
     * Assumes the 'members' array in the Team document stores Volunteer IDs.
     */
    public List<Volunteer> getTeamMembers(String teamId) throws ExecutionException, InterruptedException {
        System.out.println("TeamCustomRepo: getTeamMembers called for teamId: " + teamId);
        DocumentSnapshot teamDoc = firestore.collection(TEAMS_COLLECTION).document(teamId).get().get();

        if (!teamDoc.exists()) {
            System.err.println("TeamCustomRepo: Error: Team document not found for ID: " + teamId);
            throw new RuntimeException("Team not found: " + teamId);
        }

        Team team = teamDoc.toObject(Team.class);
        List<Volunteer> volunteers = new ArrayList<>();

        if (team.getMembers() != null && !team.getMembers().isEmpty()) {
            System.out.println("TeamCustomRepo: Processing " + team.getMembers().size() + " member IDs from team: " + team.getMembers());
            for (String memberId : team.getMembers()) { // These are now expected to be Volunteer IDs
                if (memberId == null || memberId.isBlank()) {
                    System.out.println("TeamCustomRepo: Skipping null or blank member ID.");
                    continue;
                }
                System.out.println("TeamCustomRepo: Attempting to fetch volunteer by ID: " + memberId);
                DocumentSnapshot volunteerDoc = firestore.collection(VOLUNTEERS_COLLECTION).document(memberId).get().get();

                if (volunteerDoc.exists()) {
                    Volunteer fetchedVolunteer = volunteerDoc.toObject(Volunteer.class);
                    fetchedVolunteer.setVolunteerId(volunteerDoc.getId());
                    volunteers.add(fetchedVolunteer);
                    System.out.println("TeamCustomRepo: Fetched member: " + fetchedVolunteer.getFirstName() + " " + fetchedVolunteer.getLastName() + " (ID: " + fetchedVolunteer.getVolunteerId() + ", Email: " + fetchedVolunteer.getEmail() + ")");
                } else {
                    System.out.println("TeamCustomRepo: Warning: Volunteer with ID '" + memberId + "' not found in 'volunteers' collection.");
                }
            }
        } else {
            System.out.println("TeamCustomRepo: Team has no members list or it's empty/null.");
        }
        System.out.println("TeamCustomRepo: Total full members fetched for team " + teamId + ": " + volunteers.size());
        return volunteers;
    }

    /**
     * Retrieves full Volunteer objects for all leaders of a given team.
     * Assumes the 'leaders' array in the Team document stores Volunteer IDs.
     */
    public List<Volunteer> getTeamLeaders(String teamId) throws ExecutionException, InterruptedException {
        System.out.println("TeamCustomRepo: getTeamLeaders called for teamId: " + teamId);
        DocumentSnapshot teamDoc = firestore.collection(TEAMS_COLLECTION).document(teamId).get().get();

        if (!teamDoc.exists()) {
            System.err.println("TeamCustomRepo: Error: Team document not found for ID: " + teamId);
            throw new RuntimeException("Team not found: " + teamId);
        }

        Team team = teamDoc.toObject(Team.class);
        List<Volunteer> volunteers = new ArrayList<>();

        if (team.getLeaders() != null && !team.getLeaders().isEmpty()) {
            System.out.println("TeamCustomRepo: Processing " + team.getLeaders().size() + " leader IDs from team: " + team.getLeaders());
            for (String leaderId : team.getLeaders()) { // These are now expected to be Volunteer IDs
                if (leaderId == null || leaderId.isBlank()) {
                    System.out.println("TeamCustomRepo: Skipping null or blank leader ID.");
                    continue;
                }
                System.out.println("TeamCustomRepo: Attempting to fetch leader by ID: " + leaderId);
                DocumentSnapshot volunteerDoc = firestore.collection(VOLUNTEERS_COLLECTION).document(leaderId).get().get();

                if (volunteerDoc.exists()) {
                    Volunteer fetchedVolunteer = volunteerDoc.toObject(Volunteer.class);
                    fetchedVolunteer.setVolunteerId(volunteerDoc.getId());
                    volunteers.add(fetchedVolunteer);
                    System.out.println("TeamCustomRepo: Fetched leader: " + fetchedVolunteer.getFirstName() + " " + fetchedVolunteer.getLastName() + " (ID: " + fetchedVolunteer.getVolunteerId() + ", Email: " + fetchedVolunteer.getEmail() + ")");
                } else {
                    System.out.println("TeamCustomRepo: Warning: Leader with ID '" + leaderId + "' not found in 'volunteers' collection.");
                }
            }
        } else {
            System.out.println("TeamCustomRepo: Team has no leaders list or it's empty/null.");
        }
        System.out.println("TeamCustomRepo: Total full leaders fetched for team " + teamId + ": " + volunteers.size());
        return volunteers;
    }

    /**
     * Fetches a single Volunteer document by its Firestore Document ID.
     */
    public Mono<Volunteer> getVolunteerById(String volunteerId) {
        System.out.println("TeamCustomRepo: getVolunteerById called for ID: " + volunteerId);
        return Mono.fromCallable(() -> {
            try {
                DocumentSnapshot snapshot = firestore.collection(VOLUNTEERS_COLLECTION).document(volunteerId).get().get();
                if (snapshot.exists()) {
                    Volunteer volunteer = snapshot.toObject(Volunteer.class);
                    volunteer.setVolunteerId(snapshot.getId());
                    System.out.println("TeamCustomRepo: Found volunteer by ID: " + volunteer.getFirstName() + " " + volunteer.getLastName() + " (Email: " + volunteer.getEmail() + ", ID: " + volunteer.getVolunteerId() + ")");
                    return volunteer;
                }
                System.out.println("TeamCustomRepo: Volunteer not found for ID: " + volunteerId);
                return null;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("TeamCustomRepo: Error fetching volunteer by ID: " + e.getMessage());
                throw new RuntimeException("Error fetching volunteer by ID: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Fetches a single Volunteer document by their email. Used for email-to-ID conversion.
     */
    public Mono<Volunteer> getVolunteerByEmail(String email) {
        System.out.println("TeamCustomRepo: getVolunteerByEmail called for email: " + email);
        return Mono.fromCallable(() -> {
            if (email == null || email.isBlank()) {
                System.out.println("TeamCustomRepo: Provided email is null or blank for getVolunteerByEmail.");
                return null;
            }
            QuerySnapshot querySnapshot = firestore.collection(VOLUNTEERS_COLLECTION)
                    .whereEqualTo("email", email.trim())
                    .limit(1)
                    .get()
                    .get();

            if (!querySnapshot.isEmpty()) {
                Volunteer volunteer = querySnapshot.getDocuments().get(0).toObject(Volunteer.class);
                volunteer.setVolunteerId(querySnapshot.getDocuments().get(0).getId());
                return volunteer;
            }
            System.out.println("TeamCustomRepo: Volunteer with email '" + email + "' not found.");
            return null;
        });
    }
}