package group11.volunteer_skills_bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import group11.volunteer_skills_bank.model.Message;
import group11.volunteer_skills_bank.repository.MessageRepository;
import group11.volunteer_skills_bank.utils.MessageCustomRepo;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageCustomRepo messageCustomRepo;

    public Message saveMessage(Message message) {
        return messageRepository.save(message).block();
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id).block();
    }

    public Message getLatestMessage(String chatId) {
        return messageCustomRepo.findLatestMessage(chatId);
    }

    public Message findById(String id) {
        return messageRepository.findById(id).block();
    }

    public List<Message> fetchMessages(String chatId) {
        return messageRepository.findByChatIdOrderBySentAtAsc(chatId).collectList().block();
    }

    public List<Message> getUnreadMessagesInChatByReaderId(String chatId, String string) {
        return messageCustomRepo.getUnreadMessagesInChatByReaderId(chatId, string);
    }
}
