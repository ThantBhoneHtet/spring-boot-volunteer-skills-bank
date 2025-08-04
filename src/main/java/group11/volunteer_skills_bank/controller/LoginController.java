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

import group11.volunteer_skills_bank.model.Volunteer;
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
    public ResponseEntity<?> loginOrganization(@RequestBody Map<String,String> credentials) {
        try {
            String email = credentials.get("email");
            String password= credentials.get("password");
            organizationService.login(email, password);

            return ResponseEntity.ok( "Login successful!");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }
}
