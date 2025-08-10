package group11.volunteer_skills_bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import group11.volunteer_skills_bank.dto.organization.OrganizationUpdate;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.service.OrganizationService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;


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

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" }) // Assuming 'consumes' is still multipart
    public Mono<Organization> updateVolunteer(
            @PathVariable String id,
            @RequestPart("dto") OrganizationUpdate dto,
            @RequestPart(value ="image",required=false) MultipartFile image // 'image' is now required as per signature
    ) throws IOException, ExecutionException, InterruptedException {
        return organizationService.updateOrganization(id, dto, image);
    }
    
}
