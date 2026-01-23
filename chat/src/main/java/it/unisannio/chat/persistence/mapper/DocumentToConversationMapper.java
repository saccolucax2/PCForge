package it.unisannio.chat.persistence.mapper;

import it.unisannio.chat.data.Conversation;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DocumentToConversationMapper implements Function<Document, Conversation> {

    @Override
    public Conversation apply(Document doc) {
        Conversation conversation = new Conversation();

        // Id
        if (doc.getObjectId("_id") != null) {
            conversation.setId(doc.getObjectId("_id").toHexString());
        }

        // Partecipanti
        conversation.setParticipants((List<String>) doc.get("participants"));

        // createdAt e lastMessageAt
        if (doc.get("createdAt") != null) {
            conversation.setCreatedAt(LocalDateTime.ofInstant(
                    ((Date) doc.get("createdAt")).toInstant(), ZoneOffset.UTC));
        }

        if (doc.get("lastMessageAt") != null) {
            conversation.setLastMessageAt(LocalDateTime.ofInstant(
                    ((Date) doc.get("lastMessageAt")).toInstant(), ZoneOffset.UTC));
        }

        // lastReadAtPerUser
        Map<String, LocalDateTime> lastReadMap = new HashMap<>();
        Document rawDoc = (Document) doc.get("lastReadAtPerUser");
        if (rawDoc != null) {
            rawDoc.forEach((k, v) -> {
                if (v != null) {
                    lastReadMap.put(k, LocalDateTime.ofInstant(((Date) v).toInstant(), ZoneOffset.UTC));
                }
            });
        }
        conversation.setLastReadAtPerUser(lastReadMap);

        return conversation;
    }
}
