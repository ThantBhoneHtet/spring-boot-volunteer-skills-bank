package group11.volunteer_skills_bank.model;

import com.google.cloud.spring.data.firestore.Document;

import lombok.Data;

@Data
@Document(collectionName = "analytics")
public class Analytics {
    private String date; // YYYY-MM-DD
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer newUsers;
    private Integer opportunitiesPosted;
    private Integer applicationsSubmitted;
    private Integer volunteerHoursLogged;
    private Integer organizationsRegistered;
}