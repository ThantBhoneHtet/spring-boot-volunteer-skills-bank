package group11.volunteer_skills_bank.controller;

import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.model.Team;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/volunteers")
    public ResponseEntity<List<Volunteer>> searchVolunteers(@RequestParam String q) {
        List<Volunteer> volunteers = searchService.searchVolunteers(q);
        return ResponseEntity.ok(volunteers);
    }

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> searchTeams(@RequestParam String q) {
        List<Team> teams = searchService.searchTeams(q);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> searchOrganizations(@RequestParam String q) {
        List<Organization> organizations = searchService.searchOrganizations(q);
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/all")
    public ResponseEntity<SearchResult> searchAll(@RequestParam String q) {
        SearchResult result = searchService.searchAll(q);
        return ResponseEntity.ok(result);
    }

    public static class SearchResult {
        private List<Volunteer> volunteers;
        private List<Team> teams;
        private List<Organization> organizations;
        private String query;

        public SearchResult(List<Volunteer> volunteers, List<Team> teams, List<Organization> organizations, String query) {
            this.volunteers = volunteers;
            this.teams = teams;
            this.organizations = organizations;
            this.query = query;
        }

        // Getters and setters
        public List<Volunteer> getVolunteers() { return volunteers; }
        public void setVolunteers(List<Volunteer> volunteers) { this.volunteers = volunteers; }
        public List<Team> getTeams() { return teams; }
        public void setTeams(List<Team> teams) { this.teams = teams; }
        public List<Organization> getOrganizations() { return organizations; }
        public void setOrganizations(List<Organization> organizations) { this.organizations = organizations; }
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }
}
