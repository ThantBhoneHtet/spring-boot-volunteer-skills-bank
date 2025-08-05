package group11.volunteer_skills_bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.model.Admin;
import group11.volunteer_skills_bank.repository.AdminRepo;

@Service
public class AdminService 
{
    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createAdmin(Admin admin) 
    {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreatedAt(Timestamp.now());
        adminRepo.save(admin).block();
    }

    public void login(String email, String password) 
    {
        Admin admin = adminRepo.findByEmail(email).block();
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) 
        {
            throw new IllegalArgumentException("Invalid email or password");
        }
        admin.setLastActive(Timestamp.now());
        adminRepo.save(admin).block();    
    }
}
