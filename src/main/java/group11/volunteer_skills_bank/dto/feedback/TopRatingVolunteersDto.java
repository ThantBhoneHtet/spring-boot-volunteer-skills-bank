package group11.volunteer_skills_bank.dto.feedback;

import java.util.List;
import java.util.Map;

public record TopRatingVolunteersDto (
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
        String reviewerId,
        String organizationName,
        String logoUrl,
        String feedbackText,
        int score,
        double confidence
    ) {}
}