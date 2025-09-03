package group11.volunteer_skills_bank.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import group11.volunteer_skills_bank.model.OpportunityMembers;
import group11.volunteer_skills_bank.repository.OpportunityMembersRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OpportunityMembersService {

    private final OpportunityMembersRepository opportunityMembersRepository;

    public OpportunityMembersService(OpportunityMembersRepository opportunityMembersRepository) {
        this.opportunityMembersRepository = opportunityMembersRepository;
    }

    // Add volunteer to opportunity members
    public Mono<OpportunityMembers> addVolunteerToOpportunity(String opportunityId, String volunteerId, String organizationId) {
        return opportunityMembersRepository.findByOpportunityId(opportunityId)
                .switchIfEmpty(createNewOpportunityMembers(opportunityId, organizationId))
                .flatMap(members -> {
                    List<String> volunteers = members.getVolunteers() != null ? 
                        new ArrayList<>(members.getVolunteers()) : new ArrayList<>();
                    
                    if (!volunteers.contains(volunteerId)) {
                        volunteers.add(volunteerId);
                        members.setVolunteers(volunteers);
                        return opportunityMembersRepository.save(members);
                    }
                    return Mono.just(members);
                });
    }

    // Add team to opportunity members
    public Mono<OpportunityMembers> addTeamToOpportunity(String opportunityId, String teamId, String organizationId) {
        return opportunityMembersRepository.findByOpportunityId(opportunityId)
                .switchIfEmpty(createNewOpportunityMembers(opportunityId, organizationId))
                .flatMap(members -> {
                    List<String> teams = members.getTeams() != null ? 
                        new ArrayList<>(members.getTeams()) : new ArrayList<>();
                    
                    if (!teams.contains(teamId)) {
                        teams.add(teamId);
                        members.setTeams(teams);
                        return opportunityMembersRepository.save(members);
                    }
                    return Mono.just(members);
                });
    }

    // Get opportunity members by opportunity ID
    public Mono<OpportunityMembers> getByOpportunityId(String opportunityId) {
        return opportunityMembersRepository.findByOpportunityId(opportunityId);
    }

    // Get all opportunity members by organization ID
    public Flux<OpportunityMembers> getByOrganizationId(String organizationId) {
        return opportunityMembersRepository.findByOrganizationId(organizationId);
    }

    // Create new opportunity members entry
    private Mono<OpportunityMembers> createNewOpportunityMembers(String opportunityId, String organizationId) {
        OpportunityMembers members = new OpportunityMembers();
        members.setListId(UUID.randomUUID().toString());
        members.setOpportunityId(opportunityId);
        members.setOrganizationId(organizationId);
        members.setVolunteers(new ArrayList<>());
        members.setTeams(new ArrayList<>());
        return Mono.just(members);
    }

    // Remove volunteer from opportunity
    public Mono<OpportunityMembers> removeVolunteerFromOpportunity(String opportunityId, String volunteerId) {
        return opportunityMembersRepository.findByOpportunityId(opportunityId)
                .flatMap(members -> {
                    List<String> volunteers = members.getVolunteers();
                    if (volunteers != null && volunteers.contains(volunteerId)) {
                        volunteers.remove(volunteerId);
                        members.setVolunteers(volunteers);
                        return opportunityMembersRepository.save(members);
                    }
                    return Mono.just(members);
                });
    }

    // Remove team from opportunity
    public Mono<OpportunityMembers> removeTeamFromOpportunity(String opportunityId, String teamId) {
        return opportunityMembersRepository.findByOpportunityId(opportunityId)
                .flatMap(members -> {
                    List<String> teams = members.getTeams();
                    if (teams != null && teams.contains(teamId)) {
                        teams.remove(teamId);
                        members.setTeams(teams);
                        return opportunityMembersRepository.save(members);
                    }
                    return Mono.just(members);
                });
    }
}
