package group11.volunteer_skills_bank.dto.chat;

import java.util.List;

public record ChatRequest(
    String type, // "DIRECT" or "GROUP"
    String name, // Required for GROUP
    List<String> participantIds // At least 1 for DIRECT, 2+ for GROUP
) {}
