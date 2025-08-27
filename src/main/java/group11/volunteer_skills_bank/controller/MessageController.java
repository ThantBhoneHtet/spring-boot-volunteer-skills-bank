package group11.volunteer_skills_bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.Timestamp;

import group11.volunteer_skills_bank.dto.message.MessageEditDto;
import group11.volunteer_skills_bank.dto.message.MessageRequest;
import group11.volunteer_skills_bank.model.Chat;
import group11.volunteer_skills_bank.model.Message;
import group11.volunteer_skills_bank.service.ChatService;
import group11.volunteer_skills_bank.service.MessageService;
import group11.volunteer_skills_bank.service.S3Service;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private S3Service s3Service;

    @PostMapping ("change/{chatId}/{newId}")
    public Flux<Message> changeChat(@PathVariable String chatId, @PathVariable String newId) {
       return messageService.changeChat(chatId, newId);
    }


    @MessageMapping("/chat/{chatId}/send")
    public Message sendMessage(@DestinationVariable String chatId, @Payload MessageRequest messageRequest) {
        // Check if sender(vol or org) exists in db
        // ...................................

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

        if(messageRequest.attachmentUrl() != null) {
            // System.out.println("Attachment url: "+messageRequest.attachmentUrl());
            message.setAttachment(messageRequest.attachmentUrl());
            message.setAttachmentName(messageRequest.attachmentName());
        }
        message.setSentAt(Timestamp.now());
        message.setReadBy(List.of());

        Message savedMessage = messageService.saveMessage(message);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);
        
        if(savedMessage.getContent() != "") {
            chat.setLastMessage(savedMessage.getContent());
        } else {
            chat.setLastMessage(savedMessage.getAttachmentName());
        }
        chat.setLastMessageTime(savedMessage.getSentAt());
        List<String> otherParticipants = chat.getParticipants()
            .stream()
            .filter(participant -> !participant.equals(savedMessage.getSenderId()))
            .toList();

        otherParticipants.forEach(participant -> {
            Integer unreadCount = chat.getUnreadCounts().get(participant);
            if (unreadCount == null) {
                chat.getUnreadCounts().put(participant, 1);
            } else {
                chat.getUnreadCounts().put(participant, unreadCount + 1);
            }
        });
        
        chatService.updateChat(chat);

        return savedMessage;

    }

    @PutMapping("/{messageId}")
    public ResponseEntity<?> editMessage(@PathVariable String messageId, @RequestBody MessageEditDto messageEditDto) {
        Message message = messageService.findById(messageId);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        if (messageEditDto.content() != null) message.setContent(messageEditDto.content());
        if (messageEditDto.attachmentUrl() != null) message.setAttachment(messageEditDto.attachmentUrl());
        if (messageEditDto.attachmentName() != null) message.setAttachmentName(messageEditDto.attachmentName());
        message.setEditedAt(Timestamp.now());


        Message updatedMessage = messageService.saveMessage(message);
        String chatId = updatedMessage.getChatId();

        Message latestMessage = updateLatestMessageForChat(chatId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MESSAGE_EDITED");
        payload.put("editedMessage", updatedMessage);
        payload.put("latestMessage", latestMessage);
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, payload);

        return ResponseEntity.ok(updatedMessage);

    }

    // @GetMapping("{chatId}/latest")
    // public Message getLatestMessage(@PathVariable String chatId) {
    //     return messageCustomRepo.findLatestMessage(chatId);
    // }


    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId) {
        Message messageToBeDeleted = messageService.findById(messageId);
        if (messageToBeDeleted == null) {
            return ResponseEntity.notFound().build();
        }

        messageService.deleteMessage(messageId);

        String chatId = messageToBeDeleted.getChatId();
        Message latestMessage = updateLatestMessageForChat(chatId);
        if(latestMessage == null) {
            latestMessage = new Message();
            latestMessage.setContent(null);
            latestMessage.setSentAt(null);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MESSAGE_DELETED");
        payload.put("deletedMessage", messageToBeDeleted);
        payload.put("latestMessage", latestMessage);
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, payload);
        
        return ResponseEntity.ok("Message deleted successfully.");
    }

    @GetMapping("/chat/{chatId}")
    public List<Message> fetchMessages(
        @PathVariable String chatId, 
        @RequestParam(required = false) String lastMsgId, 
        @RequestParam(defaultValue = "20") int size) {

        return messageService.fetchMessages(chatId, lastMsgId, size);
    }

    @PostMapping("/chat/{chatId}/upload-attachment")
    public String uploadAttachment(@PathVariable String chatId, @RequestPart MultipartFile attachment) {
        try{
            return s3Service.uploadFile(attachment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload attachment", e);
        }
    }

    @PutMapping("/{chatId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable String chatId, @RequestBody Map<String, String> requestBody) {
        Chat chat = chatService.getChat(chatId).block();
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        String readerId = requestBody.get("readerId");
        if (readerId == null || readerId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing readerId in request");
        }

        try {
            List<Message> unreadMessages = messageService.getUnreadMessagesInChatByReaderId(chatId, readerId);
            if (unreadMessages != null) {
                unreadMessages.forEach(msg -> {
                    msg.getReadBy().add(readerId);
                    messageService.saveMessage(msg);
                });
                chat.getUnreadCounts().put(String.valueOf(readerId), 0);
                chatService.updateChat(chat);
            }
        } catch(Exception e) {
            throw new RuntimeException("Failed to mark messages as read", e);
        }
        return ResponseEntity.ok("Messages marked as read successfully.");    
    }

    public Message updateLatestMessageForChat(String chatId) {
        Chat chat = chatService.getChat(chatId).block();
        Message latestMessage = null;
        if (chat != null) {
            latestMessage = messageService.getLatestMessage(chatId);
            if (latestMessage == null) {
                chat.setLastMessage(null);
                chat.setLastMessageTime(null);
                chatService.updateChat(chat);
                return null;
            }

            if(latestMessage.getContent() != "")
                chat.setLastMessage(latestMessage.getContent());
            else
                chat.setLastMessage(latestMessage.getAttachmentName());

            chat.setLastMessageTime(latestMessage.getSentAt());
            chatService.updateChat(chat);
        }
        return latestMessage;
    }

    @GetMapping("/{chatId}/unreadMsgs")
    public List<Message> getUnreadMessagesInChatByReaderId(@PathVariable String chatId, @RequestParam String readerId) {
        return messageService.getUnreadMessagesInChatByReaderId(chatId, readerId);
    }
}
