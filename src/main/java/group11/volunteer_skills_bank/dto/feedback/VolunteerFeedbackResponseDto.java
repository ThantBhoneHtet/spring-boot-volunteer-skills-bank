package group11.volunteer_skills_bank.dto.feedback;

import java.util.List;
import java.util.Map;

import com.google.cloud.Timestamp;

public record VolunteerFeedbackResponseDto (
    String volunteerId,
    String firstName,
    String lastName,
    String avatarUrl,
    int completedOpportunities,
    float rating,
    List<FeedbackDetail> feedbackDetails
) {
    public record FeedbackDetail(
        String opportunityId,
        String opportunityName,
        String reviewerId,
        String organizationName,
        String logoUrl,
        String feedbackText,
        Timestamp feedbackTime,
        int score,
        double confidence
    ) {}
}