package group11.volunteer_skills_bank.dto.message;

import java.util.List;

public record MessageRequest(
    String senderId, 
    String content, 
    List<String> attachments
) {}