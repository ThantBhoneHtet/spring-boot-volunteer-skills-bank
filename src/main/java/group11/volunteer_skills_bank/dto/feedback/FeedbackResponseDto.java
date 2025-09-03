package group11.volunteer_skills_bank.dto.feedback;

public record FeedbackResponseDto (
    String sentiment,
    int score,
    double confidence,
    String language,
    String content_type
) {}

