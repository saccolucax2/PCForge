package it.unisannio.chat.persistence.mapper;

import it.unisannio.chat.data.Conversation;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConversationToDocumentMapper implements Function<Conversation, Document> {

    @Override
    public Document apply(Conversation conversation) {
        Document doc = new Document();

        // Id
        if (conversation.getId() != null) {
            doc.append("_id", new ObjectId(conversation.getId()));
        }

        // Partecipanti
        doc.append("participants", conversation.getParticipants());

        // createdAt e lastMessageAt
        if (conversation.getCreatedAt() != null) {
            doc.append("createdAt", Date.from(conversation.getCreatedAt().toInstant(ZoneOffset.UTC)));
        }

        if (conversation.getLastMessageAt() != null) {
            doc.append("lastMessageAt", Date.from(conversation.getLastMessageAt().toInstant(ZoneOffset.UTC)));
        }

        // lastReadAtPerUser
        if (conversation.getLastReadAtPerUser() != null) {
            Map<String, Date> map = new HashMap<>();
            conversation.getLastReadAtPerUser().forEach((k, v) -> {
                if (v != null) {
                    map.put(k, Date.from(v.toInstant(ZoneOffset.UTC)));
                }
            });
            doc.append("lastReadAtPerUser", map);
        }

        return doc;
    }
}
