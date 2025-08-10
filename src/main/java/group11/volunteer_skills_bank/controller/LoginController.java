package group11.volunteer_skills_bank.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.dto.organization.OrganizationLoginResponse;
import group11.volunteer_skills_bank.model.Admin;
import group11.volunteer_skills_bank.model.Organization;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.service.AdminService;
import group11.volunteer_skills_bank.service.OrganizationService;
import group11.volunteer_skills_bank.service.VolunteerService;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth")
public class LoginController 
{
    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AdminService adminService;
    
    @PostMapping("volunteer/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) 
    {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Volunteer volunteer = volunteerService.login(email, password);
        System.out.println(volunteer);
        if (volunteer != null) {
            return ResponseEntity.ok(volunteer);
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
    
    @PostMapping("/organization/login")
    public ResponseEntity<?> loginOrganization(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
        
            // 1. Capture the returned object from the service
            Organization loggedInOrganization = organizationService.login(email, password);
            // 2. Create a DTO from the database entity
            OrganizationLoginResponse responseDto = new OrganizationLoginResponse(
                loggedInOrganization.getOrganizationId(), // Assumes your Organization entity has a getId() method
                loggedInOrganization.getEmail(),
                loggedInOrganization.getOrganizationName()
            );

            // 3. Return the DTO object. Spring Boot will automatically convert it to JSON.
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // It's good practice to log the full error on the server
            // log.error("An unexpected error occurred during login", e); 
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred."));
        }
    }
    
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            Admin admin=adminService.login(email, password);

            return ResponseEntity.ok(admin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred.")); 
        }
    }

    @PostMapping
    public String adminCreate(@RequestBody Admin admin) {
        adminService.createAdmin(admin);
        return "Admin created successfully";
    }
}
