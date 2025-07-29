package group11.volunteer_skills_bank.dto.chat;

import java.util.List;

import com.google.cloud.Timestamp;

public record ChatParticipantsDto (
        String id,
        String firstName,
        String lastName,
        String avatarUrl,
        Boolean isOnline,
        Timestamp lastActive
) {}

