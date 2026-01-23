package it.unisannio.chat.persistence;

import it.unisannio.chat.data.Message;
import java.util.List;

public interface MessageRepository {
    Message createMessage(Message message);
    Message findMessageById(String messageId);
    List<Message> findByConversationId(String conversationId);
    boolean deleteMessage(String messageId);
    boolean updateMessage(Message message);
}