package it.unisannio.chat.persistence;

import it.unisannio.chat.data.Conversation;
import java.util.List;

public interface ConversationRepository {
    Conversation createConversation(Conversation conversation);
    Conversation findConversation(String conversationId);
    List<Conversation> findByUserId(String userId);
    Conversation findByParticipants(String user1Id, String user2Id);
    boolean deleteConversation(String conversationId);
    boolean updateConversation(Conversation conversation);
    List<Conversation> findAll();

}