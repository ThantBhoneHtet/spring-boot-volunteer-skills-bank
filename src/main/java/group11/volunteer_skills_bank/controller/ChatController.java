package group11.volunteer_skills_bank.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import group11.volunteer_skills_bank.dto.chat.ChatParticipantsDto;
import group11.volunteer_skills_bank.dto.chat.ChatRequest;
import group11.volunteer_skills_bank.model.Chat;
import group11.volunteer_skills_bank.service.ChatService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody ChatRequest chatRequest) {
        return new ResponseEntity<>(chatService.createChat(chatRequest).block(), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public Mono<Chat> getChatById(@PathVariable String id) {
        return chatService.getChat(id);
    }

    @GetMapping("/isExisted/{participant1}/{participant2}")
    public String getChatIdBetweenParticipants(@PathVariable String participant1, @PathVariable String participant2) {
        Chat chat = chatService.getChatIdBetweenParticipants(List.of(participant1, participant2));
        if(chat != null) return chat.getChatId();
        return null;
    }

    @GetMapping("/user/{userId}")
    public Flux<Chat> getChatsByUserId(@PathVariable String userId) {
        return chatService.getChatsByUserId(userId);
    }

    @GetMapping("/{chatId}/participants")
    public List<ChatParticipantsDto> getChatParticipantsDetails(@PathVariable String chatId) {
        return chatService.getChatParticipantsDetails(chatId);
    }

    // This endpoint is not currently used in React(I commented out)
    @GetMapping("/user/{userId}/status")
    public ChatParticipantsDto getChatUserStatus(@PathVariable String userId) {
        return chatService.getChatUserStatus(userId);
    }

    @PostMapping("/online-users-count")
    public Integer getOnlineUsersCount(@RequestBody(required = false) List<String> participantIds) {
        if (participantIds != null) {
            // get online users count from user ids
            return 5;
        } 
        return 0;
    }

    @GetMapping("/online-users-count")
    public Integer getOnlineUsersCount() {
        // get all online users from db
        return 30;
    }

}
