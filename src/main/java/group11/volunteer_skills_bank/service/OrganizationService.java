package group11.volunteer_skills_bank.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.repository.OrganizationRepo;
import reactor.core.publisher.Mono;

@Service
public class OrganizationService 
{
    private OrganizationRepo organizationRepo;
    
    private OrganizationRequestService organizationRequestService;


    private PasswordEncoder passwordEncoder;

    public OrganizationService(OrganizationRepo organizationRepo, OrganizationRequestService organizationRequestService,PasswordEncoder passwordEncoder) 
    {
        this.organizationRepo = organizationRepo;
        this.organizationRequestService = organizationRequestService;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<Organization> approveRequest(String organizationId) {
        return organizationRequestService.findOrganization(organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization request not found")))
                .flatMap(request -> {
                    // Check if the request is in a pending state
                    if (!"pending".equals(request.getStatus())) {
                        return Mono.error(new IllegalStateException("Request is not in a pending state"));
                    }

                    // 1. Update the status of the request and save it first.
                    request.setStatus("approved");
                    return organizationRequestService.saveOrganizationRequest(request)
                            .flatMap(updatedRequest -> {
                                // 2. Create and save the new Organization from the updated request data
                                Organization organization = new Organization();
                                organization.setOrganizationId(updatedRequest.getOrganizationId());
                                organization.setOrganizationName(updatedRequest.getOrganizationName());
                                organization.setOrganizationType(updatedRequest.getOrganizationType());
                                organization.setEmail(updatedRequest.getEmail());
                                organization.setPassword(updatedRequest.getPassword()); // Hashed password is moved here
                                organization.setAddress(updatedRequest.getAddress());
                                organization.setDescription(updatedRequest.getDescription());
                                organization.setCreatedAt(updatedRequest.getCreatedAt());
                                organization.setStatus("active");

                                return organizationRepo.save(organization);
                            });
                });
    }

    public void login(String email, String password) {
        Organization organization = organizationRepo.findByEmail(email).block();
        if (organization == null || !passwordEncoder.matches(password, organization.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        organization.setLastLogin(Timestamp.now());
        organizationRepo.save(organization).block();
    }
}
