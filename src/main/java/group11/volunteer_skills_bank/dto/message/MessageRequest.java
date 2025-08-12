package group11.volunteer_skills_bank.dto.message;

public record MessageRequest(
    String senderId, 
    String content, 
    String attachmentUrl,
    String attachmentName
) {}