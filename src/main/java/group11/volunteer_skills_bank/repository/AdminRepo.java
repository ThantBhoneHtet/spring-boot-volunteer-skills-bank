package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;

import group11.volunteer_skills_bank.model.Admin;

@Repository
public interface AdminRepo extends FirestoreReactiveRepository<Admin> 
{
    
}
