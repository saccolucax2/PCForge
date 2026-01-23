package it.unisannio.chat.persistence.mapper;

import it.unisannio.chat.data.Message;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Function;

public class DocumentToMessageMapper implements Function<Document, Message> {
    @Override
    public Message apply(Document doc) {
        Message message = new Message();
        message.setId(doc.getObjectId("_id").toHexString());
        message.setConversationId(doc.getString("conversationId"));
        message.setFromUserId(doc.getString("fromUserId"));
        message.setContent(doc.getString("content"));

        // Correzione: leggere come Date e convertire in LocalDateTime
        Date timestamp = doc.getDate("timestamp");
        if (timestamp != null) {
            message.setTimestamp(LocalDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC));
        }

        return message;
    }
}

