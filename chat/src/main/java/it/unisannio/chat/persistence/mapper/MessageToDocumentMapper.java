package it.unisannio.chat.persistence.mapper;

import it.unisannio.chat.data.Message;
import org.bson.Document;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Function;

public class MessageToDocumentMapper implements Function<Message, Document> {
    @Override
    public Document apply(Message message) {
        Document doc = new Document();
        if (message.getId() != null) {
            doc.append("_id", new org.bson.types.ObjectId(message.getId()));
        }
        doc.append("conversationId", message.getConversationId())
                .append("fromUserId", message.getFromUserId())
                .append("content", message.getContent());

        // ✅ converti LocalDateTime in java.util.Date
        if (message.getTimestamp() != null) {
            Date timestamp = Date.from(message.getTimestamp().toInstant(ZoneOffset.UTC));
            doc.append("timestamp", timestamp);
        }

        return doc;
    }
}
