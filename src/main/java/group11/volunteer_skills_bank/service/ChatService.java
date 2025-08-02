package group11.volunteer_skills_bank.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.dto.chat.ChatParticipantsDto;
import group11.volunteer_skills_bank.dto.chat.ChatRequest;
import group11.volunteer_skills_bank.model.Chat;
import group11.volunteer_skills_bank.model.Volunteer;
import group11.volunteer_skills_bank.repository.ChatRepository;
import group11.volunteer_skills_bank.utils.ChatCustomRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatService {
    
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatCustomRepo chatCustomRepo;

    @Autowired
    private VolunteerService volunteerService;

    public Mono<Chat> createChat(ChatRequest chatRequest) {
        Chat chat = new Chat();
        chat.setCreatedAt(Timestamp.now());
        chat.setType(chatRequest.type());
        chat.setParticipants(chatRequest.participantIds());
        Map<String, Integer> unreadCounts = new HashMap<>(Map.of(
            chatRequest.participantIds().get(0), 0,
            chatRequest.participantIds().get(1), 0
        ));
        chat.setUnreadCounts(unreadCounts);
        chat.setType(chatRequest.type());

        return chatRepository.save(chat);
    }

    public Mono<Chat> getChat(String id) {
        return chatRepository.findById(id);
    }

    public Flux<Chat> getChatsByUserId(String userId) {
        return chatRepository.findByParticipantsContaining(userId);
    }

    public String getChatIdBetweenParticipants(String participant1, String participant2) {
        return chatCustomRepo.getChatIdBetweenParticipants(participant1, participant2);
    }

    public List<ChatParticipantsDto> getChatParticipantsDetails(String chatId) {
        Chat chat = chatRepository.findById(chatId).block();
        if (chat == null) return null;

        List<ChatParticipantsDto> participants = chat.getParticipants().stream()
            .map(id -> volunteerService.getVolunteer(id).block())
            .map(v -> new ChatParticipantsDto(
                v.getVolunteerId(),
                v.getFirstName(),
                v.getLastName(),
                v.getAvatarUrl(),
                v.getIsOnline(),
                v.getLastActive()
            ))
            .collect(Collectors.toList());

        return participants;
    }

    public ChatParticipantsDto getChatUserStatus(String userId) {
        Volunteer volunteer = volunteerService.getVolunteer(userId).block();
        if (volunteer == null) return null;

        return new ChatParticipantsDto(
            volunteer.getVolunteerId(),
            volunteer.getFirstName(),
            volunteer.getLastName(),
            volunteer.getAvatarUrl(),
            volunteer.getIsOnline(),
            volunteer.getLastActive()
        );
    }


    public Mono<Void> deleteChat(String id) {
        return chatRepository.deleteById(id);
    }

}
