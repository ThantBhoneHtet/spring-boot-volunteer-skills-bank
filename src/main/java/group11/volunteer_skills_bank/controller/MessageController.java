package group11.volunteer_skills_bank.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.dto.message.MessageRequest;
import group11.volunteer_skills_bank.model.Chat;
import group11.volunteer_skills_bank.model.Message;
import group11.volunteer_skills_bank.service.ChatService;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/{chatId}/send}")
    public Message sendMessage(@DestinationVariable String chatId, @Payload MessageRequest messageRequest) {
        // Check if sender(vol or org) exists in db

        // Check if chat exists in db
        Chat chat = chatService.getChat(chatId).block();
        if (chat == null) {
            throw new IllegalArgumentException("Chat not found");
        }

        Message message = new Message();
        message.setChatId(chatId);
        message.setSenderId(messageRequest.senderId());
        // message.setRecipientIds(messageRequest.recipientIds());
        message.setContent(messageRequest.content());
        message.setAttachments(messageRequest.attachments());
        message.setSendAt(Timestamp.now());
        message.setReadBy(List.of());

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, message);
        return message;

    }
}
