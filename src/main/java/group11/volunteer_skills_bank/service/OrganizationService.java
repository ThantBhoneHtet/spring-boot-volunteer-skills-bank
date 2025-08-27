package group11.volunteer_skills_bank.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import group11.volunteer_skills_bank.dto.organization.OrganizationUpdate;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.repository.OrganizationRepo;
import reactor.core.publisher.Mono;

@Service
public class OrganizationService {
    private OrganizationRepo organizationRepo;
    private OrganizationRequestService organizationRequestService;
    private PasswordEncoder passwordEncoder;
    private S3Service s3Service;
    @Autowired
    private Firestore firestore;

    public OrganizationService(OrganizationRepo organizationRepo, OrganizationRequestService organizationRequestService,
            PasswordEncoder passwordEncoder, S3Service s3Service) {
        this.organizationRepo = organizationRepo;
        this.organizationRequestService = organizationRequestService;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
    }

    public Mono<Organization> approveRequest(String organizationId) {
        return organizationRequestService.findOrganization(organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization request not found")))
                .flatMap(request -> {
                    // Check if the request is in a pending state
                    if (!"pending".equals(request.getStatus()) && !"rejected".equals(request.getStatus())) {
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
                                organization.setLocation(new Organization.Location(
                                        updatedRequest.getLocation().getCity(),
                                        updatedRequest.getLocation().getCountry()));
                                organization.setDescription(updatedRequest.getDescription());
                                organization.setCreatedAt(updatedRequest.getCreatedAt());
                                organization.setStatus("active");

                                return organizationRepo.save(organization);
                            });
                });
    }

    public Organization login(String email, String password) {
        Organization organization = organizationRepo.findByEmail(email).block();
        if (organization == null || !passwordEncoder.matches(password, organization.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        organization.setLastLogin(Timestamp.now());
        Organization savedOrganization = organizationRepo.save(organization).block();
        return savedOrganization;
    }

    public Mono<Organization> findOrganizationByEmail(String email) {
        return organizationRepo.findByEmail(email);
    }

    public Mono<Organization> findOrganization(String organizationId) {
        return organizationRepo.findById(organizationId);
    }

    public void logout(String id) {
        Organization organization = organizationRepo.findById(id).block();
        if (organization != null) {
            organization.setLastLogin(null);
            organizationRepo.save(organization).block();
        }
    }

    public Mono<Organization> updateOrganization(String id, OrganizationUpdate dto, MultipartFile image)
            throws IOException, ExecutionException, InterruptedException {
        Organization organization = organizationRepo.findById(id).block();
        if (organization == null) {
            throw new RuntimeException("Volunteer with ID " + id + " not found.");
        }
        if (image != null) {
            // Optional: Consider deleting the old image from S3 if the URL is changing.
            // This would require fetching the old avatarUrl before setting the new one.
            String newImageUrl = s3Service.uploadFile(image);
            organization.setLogoUrl(newImageUrl); // Set the new URL
        }

        if (dto.location() != null) {
            String city = dto.location().city() != null ? dto.location().city()
                    : (organization.getLocation() != null ? organization.getLocation().getCity() : null);
            String country = dto.location().country() != null ? dto.location().country()
                    : (organization.getLocation() != null ? organization.getLocation().getCountry() : null);
            organization.setLocation(new Organization.Location(city, country));
        }
        if (dto.discription() != null) {
            organization.setDescription(dto.discription());
        }
        if (dto.organizationName() != null) {
            organization.setOrganizationName(dto.organizationName());
        }
        if (dto.organizationType() != null) {
            organization.setOrganizationType(dto.organizationType());
        }
        if (dto.website() != null) {
            organization.setWebsite(dto.website());
        }
        if (dto.phoneNumber() != null) {
            organization.setPhoneNumber(dto.phoneNumber());
        }
        ApiFuture<WriteResult> future = firestore.collection("organizations").document(id).set(organization);
        future.get(); // Wait for the write to complete
        return organizationRepo.save(organization);
    }
    public void saveOrganization(Organization organization) {
        organizationRepo.save(organization).block();
    }

}
