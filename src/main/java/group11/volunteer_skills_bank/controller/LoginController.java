package group11.volunteer_skills_bank.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.service.VolunteerService;

@RestController
@CrossOrigin("*")
public class LoginController 
{
    @Autowired
    private VolunteerService volunteerService;
    
    @PostMapping("/login")
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
}
