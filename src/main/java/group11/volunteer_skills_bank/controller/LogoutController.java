package group11.volunteer_skills_bank.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.service.AdminService;
import group11.volunteer_skills_bank.service.OrganizationService;
import group11.volunteer_skills_bank.service.VolunteerService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class LogoutController 
{
    private VolunteerService volunteerService;
    private OrganizationService organizationService;
    private AdminService adminService;

    public LogoutController(VolunteerService volunteerService, OrganizationService organizationService, AdminService adminService) 
    {
        this.volunteerService = volunteerService;
        this.organizationService = organizationService;
        this.adminService = adminService;
    }

    @PostMapping("/organizations/logout")
    public ResponseEntity<String> logoutOrganization(@RequestBody Map<String, String> credentials) 
    {
        String organizationId = credentials.get("organizationId");
        organizationService.logout(organizationId);
        return ResponseEntity.ok("Logout successful!");
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<String> logoutAdmin(@RequestBody Map<String, String> credentials) 
    {
        String adminId = credentials.get("adminId");
        adminService.logout(adminId);
        return ResponseEntity.ok("Logout successful!");
    }

    @PostMapping("/volunteers/logout")
    public ResponseEntity<String> logoutVolunteer(@RequestBody Map<String, String> credentials) 
    {
        String volunteerId = credentials.get("volunteerId");
        volunteerService.logout(volunteerId);
        return ResponseEntity.ok("Logout successful!");
    }
    
}
