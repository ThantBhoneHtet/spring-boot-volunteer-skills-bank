package group11.volunteer_skills_bank.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.dto.feedback.FeedbackDto;
import group11.volunteer_skills_bank.dto.feedback.FeedbackResponseDto;
import group11.volunteer_skills_bank.dto.feedback.VolunteerFeedbackResponseDto;
import group11.volunteer_skills_bank.model.Feedback;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.model.Feedback.FeedbackDetails;
import group11.volunteer_skills_bank.repository.FeedbackRepo;
import group11.volunteer_skills_bank.utils.FeedbackCustomRepo;
import group11.volunteer_skills_bank.model.Opportunity;
import group11.volunteer_skills_bank.model.Organization;

@Service
public class FeedbackService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FeedbackRepo feedbackRepo;

    @Autowired
    private FeedbackCustomRepo feedbackCustomRepo;

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OpportunityCompleteService opportunityCompleteService;

    public Feedback handleFeedback(String opportunityId, FeedbackDto feedbackDto) {

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", "2Uta2Bt7tmyU5fPlYxD4ytQDKrUqxXZr");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Prepare request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", feedbackDto.feedback());
        
        // Create the HTTP entity
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
       
        // Make the API call
        ResponseEntity<FeedbackResponseDto> response = restTemplate.exchange(
            "https://api.apilayer.com/sentiment/analysis",
            HttpMethod.POST,
            entity,
            FeedbackResponseDto.class
        );
        
        FeedbackResponseDto feedbackResponse = response.getBody();

        FeedbackDetails feedbackDetails = new FeedbackDetails();
        feedbackDetails.setOpportunityId(opportunityId);
        feedbackDetails.setReviewerId(feedbackDto.reviewerId());
        feedbackDetails.setFeedbackTime(Timestamp.now());
        feedbackDetails.setFeedbackText(feedbackDto.feedback());
        if (feedbackResponse != null) {
            feedbackDetails.setScore(feedbackResponse.score());
            feedbackDetails.setConfidence(feedbackResponse.confidence());
        }

        Feedback feedback = feedbackRepo.findById(feedbackDto.revieweeId()).block();

        if(feedback == null) {
            feedback = new Feedback();
            feedback.setFeedbackId(feedbackDto.revieweeId());
            feedback.setFeedbackDetialsList(new ArrayList<>());
        }
        feedback.getFeedbackDetialsList().add(feedbackDetails);

        List<FeedbackDetails> details = feedback.getFeedbackDetialsList();

        // simple average
        double simpleAvg = details.stream()
                .mapToInt(FeedbackDetails::getScore)
                .average()
                .orElse(0.0);

        // confidence-weighted average
        double weightedSum = details.stream()
                .mapToDouble(d -> d.getScore() * d.getConfidence())
                .sum();

        double confidenceSum = details.stream()
                .mapToDouble(FeedbackDetails::getConfidence)
                .sum();

        double weightedAvg = confidenceSum > 0 ? weightedSum / confidenceSum : 0.0;

        // ✅ store both
        feedback.setAverageScore((float) simpleAvg);
        feedback.setWeightedAverageScore((float) weightedAvg);

        Volunteer volunteer = volunteerService.findVolunteerById(feedbackDto.revieweeId()).block();
        if(volunteer != null) {
            volunteer.setRating((float) weightedAvg);
            volunteerService.saveVolunteer(volunteer);
        }

        return feedbackRepo.save(feedback).block();
    }

    public Feedback getFeedbackById(String feedbackId) {
        return feedbackRepo.findById(feedbackId).block();
    }

    public List<Feedback> getTopRatedVolunteers(Map<String, Object> filters) {
        List<String> skills = new ArrayList<>();
        Integer minCompletedOpportunities = 0;
        Float minRating = 0.0f;
        if(filters != null) {
            skills = (List<String>) filters.getOrDefault("skills", new ArrayList<String>());
            minCompletedOpportunities = (Integer) filters.getOrDefault("minCompletedOpportunities", 0);
            minRating = ((Number) filters.getOrDefault("minRating", 0.0)).floatValue();
        }
        return feedbackCustomRepo.findTopRatedFeedbacks(skills, minCompletedOpportunities, minRating);
    }

    public Feedback getFeedbacksForVolunteer(String volunteerId) {
        System.out.println("Fetching feedbacks for volunteerId: " + volunteerId);
        return feedbackRepo.findById(volunteerId).block();
    }

    public List<VolunteerFeedbackResponseDto.FeedbackDetail> mapFeedbackDetails(Feedback feedback) {
         List<VolunteerFeedbackResponseDto.FeedbackDetail> feedbackDetails =
                        Optional.ofNullable(feedback.getFeedbackDetialsList())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(detail -> {
                                    System.out.println("Processing feedbackDetail for opportunityId: " + detail.getOpportunityId());
                                    Organization org = organizationService.findOrganization(detail.getReviewerId())
                                                                        .blockOptional()
                                                                        .orElse(null);
                                    if (org == null) {
                                        System.out.println("Organization not found for reviewerId: " + detail.getReviewerId());
                                    }
                                    Opportunity opportunity = opportunityCompleteService.getOpportunity(detail.getOpportunityId());
                                    return new VolunteerFeedbackResponseDto.FeedbackDetail(
                                        detail.getOpportunityId(),
                                        opportunity != null ? opportunity.getTitle() : null,
                                        detail.getReviewerId(),
                                        org != null ? org.getOrganizationName() : null,
                                        org != null ? org.getLogoUrl() : null,
                                        detail.getFeedbackText(),
                                        detail.getFeedbackTime(),
                                        detail.getScore(),
                                        detail.getConfidence()
                                    );
                                })
                                .toList();
        return feedbackDetails;
    }
}
