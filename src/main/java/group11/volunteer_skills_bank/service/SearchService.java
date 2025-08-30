package group11.volunteer_skills_bank.service;

import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.controller.SearchController;
import group11.volunteer_skills_bank.model.Team;
import group11.volunteer_skills_bank.repository.VolunteerRepository;
import group11.volunteer_skills_bank.repository.TeamRepository;
import group11.volunteer_skills_bank.repository.OrganizationRepo; // Corrected import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final VolunteerRepository volunteerRepository;
    private final TeamRepository teamRepository;
    private final OrganizationRepo organizationRepo; // Corrected dependency name

    public List<Volunteer> searchVolunteers(String query) {
        String searchTerm = query.toLowerCase().trim();
        
        return volunteerRepository.findAll().toStream()
                .filter(volunteer -> 
                    volunteer.getEmail() != null && volunteer.getEmail().toLowerCase().contains(searchTerm) ||
                    volunteer.getFirstName() != null && volunteer.getFirstName().toLowerCase().contains(searchTerm) ||
                    volunteer.getLastName() != null && volunteer.getLastName().toLowerCase().contains(searchTerm)
                )
                .collect(Collectors.toList());
    }

    public List<Team> searchTeams(String query) {
        String searchTerm = query.toLowerCase().trim();
        
        return teamRepository.findAll().toStream()
                .filter(team -> 
                    team.getName() != null && team.getName().toLowerCase().contains(searchTerm) ||
                    team.getDescription() != null && team.getDescription().toLowerCase().contains(searchTerm)
                )
                .collect(Collectors.toList());
    }
    
    // Updated method to search for organizations only by name
    public List<Organization> searchOrganizations(String query) {
        String searchTerm = query.toLowerCase().trim();

        return organizationRepo.findAll().toStream()
                .filter(organization ->
                    organization.getOrganizationName() != null && organization.getOrganizationName().toLowerCase().contains(searchTerm)
                )
                .collect(Collectors.toList());
    }

    public SearchController.SearchResult searchAll(String query) {
        List<Volunteer> volunteers = searchVolunteers(query);
        List<Team> teams = searchTeams(query);
        List<Organization> organizations = searchOrganizations(query);
        
        return new SearchController.SearchResult(volunteers, teams, organizations, query);
    }
}
