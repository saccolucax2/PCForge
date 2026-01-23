package it.unisannio.chat.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import it.unisannio.chat.data.Message;
import it.unisannio.chat.persistence.mapper.MessageToDocumentMapper;
import it.unisannio.chat.persistence.mapper.DocumentToMessageMapper;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import static com.mongodb.client.model.Filters.eq;

public class MessageRepositoryMongo implements MessageRepository {

    private final MongoClient mongoClient;
    private final Function<Message, Document> messageToDocumentMapper = new MessageToDocumentMapper();
    private final Function<Document, Message> documentToMessageMapper = new DocumentToMessageMapper();

    private static MessageRepositoryMongo INSTANCE;

    private MessageRepositoryMongo() {
        String host = System.getenv("MONGO_ADDRESS") != null ? System.getenv("MONGO_ADDRESS") : "localhost";
        String port = System.getenv("MONGO_PORT") != null ? System.getenv("MONGO_PORT") : "27017";
        mongoClient = MongoClients.create("mongodb://" + host + ":" + port);
    }

    public static MessageRepositoryMongo getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MessageRepositoryMongo();
        }
        return INSTANCE;
    }

    private MongoCollection<Document> getCollection() {
        MongoDatabase db = mongoClient.getDatabase("chatdb");
        return db.getCollection("messages");
    }

    @Override
    public Message createMessage(Message message) {
        Document doc = messageToDocumentMapper.apply(message);
        getCollection().insertOne(doc);
        message.setId(doc.getObjectId("_id").toHexString());
        return message;
    }

    @Override
    public Message findMessageById(String messageId) {
        Document doc = getCollection().find(eq("_id", new ObjectId(messageId))).first();
        return doc != null ? documentToMessageMapper.apply(doc) : null;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<Message> list = new ArrayList<>();
        getCollection()
                .find(eq("conversationId", conversationId))
                .map(documentToMessageMapper::apply)
                .into(list);
        return list;
    }

    @Override
    public boolean deleteMessage(String messageId) {
        DeleteResult result = getCollection().deleteOne(eq("_id", new ObjectId(messageId)));
        return result.getDeletedCount() > 0;
    }

    @Override
    public boolean updateMessage(Message message) {
        try {
            Document filter = new Document("_id", new ObjectId(message.getId()));

            Document updateFields = new Document()
                    .append("content", message.getContent());

            // ✅ converti LocalDateTime in Date prima di salvare
            if (message.getTimestamp() != null) {
                updateFields.append("timestamp", Date.from(message.getTimestamp().toInstant(ZoneOffset.UTC)));
            }

            Document update = new Document("$set", updateFields);
            getCollection().updateOne(filter, update);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}