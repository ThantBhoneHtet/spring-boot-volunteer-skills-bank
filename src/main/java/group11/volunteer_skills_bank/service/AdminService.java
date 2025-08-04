package group11.volunteer_skills_bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.model.Admin;
import group11.volunteer_skills_bank.repository.AdminRepo;

@Service
public class AdminService 
{
    @Autowired
    private AdminRepo adminRepo;

    public void createAdmin(Admin admin) 
    {
        admin.setCreatedAt(Timestamp.now());
        adminRepo.save(admin).block();
    }
}
