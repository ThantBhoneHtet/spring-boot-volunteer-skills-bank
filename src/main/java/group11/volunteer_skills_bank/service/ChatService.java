package group11.volunteer_skills_bank.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.WriteResult;

import group11.volunteer_skills_bank.dto.chat.ChatParticipantsDto;
import group11.volunteer_skills_bank.dto.chat.ChatRequest;
import group11.volunteer_skills_bank.model.Chat;
import group11.volunteer_skills_bank.model.Organization;
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

    @Autowired
    private OrganizationService organizationService;

    public Mono<Chat> createChat(ChatRequest chatRequest) {
        Chat chat = new Chat();
        chat.setCreatedAt(Timestamp.now());
        chat.setType(chatRequest.type());
        chat.setParticipants(chatRequest.participantIds());

        List<String> participants = new ArrayList<>(chatRequest.participantIds());
        Collections.sort(participants);
        String key = String.join("_", participants);
        String hashedChatId = DigestUtils.sha256Hex(key);
        chat.setChatId(hashedChatId);

        Map<String, Integer> unreadCounts = new HashMap<>();
        participants.forEach(p -> {
            unreadCounts.put(p, 0);
        });
        chat.setUnreadCounts(unreadCounts);

        chat.setType(chatRequest.type());

        if(chatRequest.name() != null) chat.setGroupName(chatRequest.name());

        return chatRepository.save(chat);
    }

    public Mono<Chat> getChat(String id) {
        return chatRepository.findById(id);
    }

    public Flux<Chat> getChatsByUserId(String userId) {
        return chatRepository.findByParticipantsContaining(userId);
    }

    public Chat getChatIdBetweenParticipants(List<String> participantIds) {
        List<String> sorted = new ArrayList<>(participantIds);
        Collections.sort(sorted);
        String key = String.join("_", sorted);
        String chatId = DigestUtils.sha256Hex(key);
        return chatRepository.findById(chatId).block();
        // return chatCustomRepo.getChatIdBetweenParticipants(participantIds);
    }

    public List<ChatParticipantsDto> getChatParticipantsDetails(String chatId) {
        Chat chat = chatRepository.findById(chatId).block();
        if (chat == null) return null;

        List<ChatParticipantsDto> participants = chat.getParticipants().stream()
        .map(id -> {
            Volunteer v = volunteerService.getVolunteer(id).block();
            if (v != null) {
                return new ChatParticipantsDto(
                    v.getVolunteerId(),
                    v.getFirstName(),
                    v.getLastName(),
                    v.getAvatarUrl(),
                    v.getIsOnline(),
                    v.getLastActive()
                );
            }

            Organization o = organizationService.findOrganization(id).block();
            if (o != null) {
                return new ChatParticipantsDto(
                    o.getOrganizationId(),
                    o.getOrganizationName(),
                    null, // last name not applicable
                    o.getLogoUrl(),
                    o.getIsOnline(),
                    null
                );
            }

            return null; // participant not found in both
        })
        .filter(Objects::nonNull)
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

    public void updateChat(Chat chat) {
        chatCustomRepo.updateChat(chat);
    }

    public Mono<Void> deleteChat(String id) {
        return chatRepository.deleteById(id);
    }

}
