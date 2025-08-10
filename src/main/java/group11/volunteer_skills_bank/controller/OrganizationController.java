package group11.volunteer_skills_bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.service.OrganizationService;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/organizations")
@CrossOrigin("*")
public class OrganizationController 
{
    private OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }
    @GetMapping("/{id}")
    public Mono<Organization> getMethodName(@PathVariable String id) {
        return organizationService.findOrganization(id);
    }


    
}
