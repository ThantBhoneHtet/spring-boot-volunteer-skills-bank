package group11.volunteer_skills_bank.dto.feedback;

public record FeedbackDto (
    String revieweeId,
    String reviewerId,
    String feedback
) {}
