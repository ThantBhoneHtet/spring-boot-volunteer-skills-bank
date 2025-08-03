package group11.volunteer_skills_bank.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;

import group11.volunteer_skills_bank.model.Organization;

@Repository
public interface OrganizationRepo extends FirestoreReactiveRepository<Organization> 
{

}
