package group11.volunteer_skills_bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.dto.organization.OrganizationRequestDto;
import group11.volunteer_skills_bank.model.OrganizationRequest;
import group11.volunteer_skills_bank.repository.OrganizationRequestRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrganizationRequestService {
    @Autowired
    private OrganizationRequestRepo organizationRequestRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> createOrganizationRequest(OrganizationRequestDto organizationRequestDto) {
        if (Boolean.TRUE.equals(organizationRequestRepo.existsByEmail(organizationRequestDto.email()).block())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setOrganizationId(organizationRequestDto.organizationId());
        organizationRequest.setEmail(organizationRequestDto.email());
        organizationRequest.setOrganizationName(organizationRequestDto.organizationName());
        organizationRequest.setOrganizationType(organizationRequestDto.organizationType());
        organizationRequest.setLocation(new OrganizationRequest.Location(
                organizationRequestDto.location().country(),
                organizationRequestDto.location().city()));
        organizationRequest.setPassword(passwordEncoder.encode(organizationRequestDto.password()));
        organizationRequest.setDescription(organizationRequestDto.description());
        organizationRequest.setStatus("pending");
        organizationRequest.setCreatedAt(Timestamp.now());
        organizationRequestRepo.save(organizationRequest).block();
        return ResponseEntity.ok("Organization request created successfully");
    }

    public Flux<OrganizationRequest> getAllOrganizationRequests() {
        return organizationRequestRepo.findAll();
    }

    public Mono<OrganizationRequest> rejectOrganizationRequest(String organizationId, String rejectionReason) {
        return organizationRequestRepo.findByOrganizationId(organizationId)
                .flatMap(request -> {
                    if (!"pending".equals(request.getStatus())) {
                        return Mono.error(new IllegalStateException("Request is not in a pending state"));
                    }

                    // Update the request status to 'rejected'
                    request.setStatus("rejected");
                    request.setRejectionReason(rejectionReason);

                    return organizationRequestRepo.save(request);
                });
    }

    public Mono<OrganizationRequest> findOrganization(String organizationId) {
        return organizationRequestRepo.findByOrganizationId(organizationId);
    }

    public Mono<OrganizationRequest> saveOrganizationRequest(OrganizationRequest organizationRequest) {
        return organizationRequestRepo.save(organizationRequest);
    }

}
