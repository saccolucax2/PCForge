package it.unisannio.chat.application;

import it.unisannio.chat.data.Conversation;
import it.unisannio.chat.data.Message;
import it.unisannio.chat.persistence.ConversationRepository;
import it.unisannio.chat.persistence.ConversationRepositoryMongo;
import it.unisannio.chat.persistence.MessageRepository;
import it.unisannio.chat.persistence.MessageRepositoryMongo;
import it.unisannio.chat.util.CryptoUtil;
import java.time.LocalDateTime;
import java.util.*;

public class ChatService {

    private final ConversationRepository conversationRepository= ConversationRepositoryMongo.getInstance();
    private final MessageRepository messageRepository= MessageRepositoryMongo.getInstance();
    private final CryptoUtil cryptoUtil= CryptoUtil.getInstance();

    public ChatService() {

    }

    public Conversation createConversation(String user1Id, String user2Id) {
        Conversation existing = conversationRepository.findByParticipants(user1Id, user2Id);
        if (existing != null) return existing;

        Conversation conversation = new Conversation();
        conversation.setParticipants(List.of(user1Id, user2Id));
        LocalDateTime now = LocalDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setLastMessageAt(now);

        Map<String, LocalDateTime> lastRead = new HashMap<>();
        lastRead.put(user1Id, now);
        lastRead.put(user2Id, now); // meglio non usare MIN, rischi overflow
        conversation.setLastReadAtPerUser(lastRead);

        return conversationRepository.createConversation(conversation);
    }

    public Message sendMessage(String conversationId, String fromUserId, String content) {
        Conversation conversation = conversationRepository.findConversation(conversationId);
        if (conversation == null) throw new IllegalArgumentException("Conversation not found");

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setFromUserId(fromUserId);

        // 🔐 Cripta il contenuto prima di salvarlo
        String encrypted = cryptoUtil.encrypt(content);
        message.setContent(encrypted);

        message.setTimestamp(LocalDateTime.now());
        Message saved = messageRepository.createMessage(message);

        conversation.setLastMessageAt(saved.getTimestamp());
        conversationRepository.updateConversation(conversation);

        // 🔓 Decifra il contenuto per la risposta
        saved.setContent(content);
        return saved;
    }

    public List<Message> getMessages(String conversationId) {
        List<Message> encryptedMessages = messageRepository.findByConversationId(conversationId);

        // 🔓 Decifra tutti i messaggi prima di restituirli
        for (Message m : encryptedMessages) {
            try {
                m.setContent(cryptoUtil.decrypt(m.getContent()));
            } catch (Exception e) {
                m.setContent("[Error decrypting]");
            }
        }
        return encryptedMessages;
    }

    public List<Conversation> getConversationsForUser(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    public List<String> getConversationParticipants(String conversationId) {
        Conversation conversation = conversationRepository.findConversation(conversationId);
        if (conversation == null) throw new IllegalArgumentException("Conversation not found");
        return conversation.getParticipants();
    }

    public void markMessagesAsRead(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findConversation(conversationId);
        if (conversation == null) throw new IllegalArgumentException("Conversation not found");

        conversation.getLastReadAtPerUser().put(userId, LocalDateTime.now());
        conversationRepository.updateConversation(conversation);
    }

    public long countUnreadMessages(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findConversation(conversationId);
        if (conversation == null) throw new IllegalArgumentException("Conversation not found");

        LocalDateTime lastRead = conversation.getLastReadAtPerUser().getOrDefault(userId, LocalDateTime.MIN);
        List<Message> allMessages = messageRepository.findByConversationId(conversationId);

        return allMessages.stream()
                .filter(msg -> !msg.getFromUserId().equals(userId))
                .filter(msg -> msg.getTimestamp().isAfter(lastRead))
                .count();
    }

    public boolean deleteConversation(String conversationId) {
        List<Message> messages = messageRepository.findByConversationId(conversationId);
        for (Message m : messages) {
            messageRepository.deleteMessage(m.getId());
        }
        return conversationRepository.deleteConversation(conversationId);
    }
}