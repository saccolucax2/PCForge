package it.unisannio.forum.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.unisannio.forum.model.Comment;
import it.unisannio.forum.persistence.mapper.CommentToDocumentMapper;
import it.unisannio.forum.persistence.mapper.DocumentToCommentMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.DeleteResult;
import java.util.function.Function;
import org.bson.Document;
import org.bson.types.ObjectId;

public class CommentRepositoryMongo implements CommentRepository {

    private final MongoClient mongoClient;
    private final Function<Comment, Document> toDocument = new CommentToDocumentMapper();
    private final Function<Document, Comment> toComment = new DocumentToCommentMapper();

    private static CommentRepositoryMongo INSTANCE;

    private CommentRepositoryMongo() {
        String host = System.getenv("MONGO_ADDRESS") != null ? System.getenv("MONGO_ADDRESS") : "localhost";
        String port = System.getenv("MONGO_PORT") != null ? System.getenv("MONGO_PORT") : "27017";
        mongoClient = MongoClients.create("mongodb://" + host + ":" + port);
    }

    public static CommentRepositoryMongo getInstance() {
        if (INSTANCE == null) INSTANCE = new CommentRepositoryMongo();
        return INSTANCE;
    }

    private MongoCollection<Document> getCollection() {
        return mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
    }

    @Override
    public Comment save(Comment comment) {
        Document doc = toDocument.apply(comment);
        InsertOneResult result = getCollection().insertOne(doc);
        if (!result.wasAcknowledged()) throw new RuntimeException("Failed to insert comment");

        comment.setId(doc.getObjectId("_id").toHexString());
        return comment;
    }

    @Override
    public Optional<Comment> findById(String id) {
        Document doc = getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(doc).map(toComment);
    }

    @Override
    public List<Comment> findByTarget(String targetType, String targetId) {
        List<Comment> comments = new ArrayList<>();
        getCollection().find(Filters.and(
                Filters.eq(TARGETTYPE, targetType),
                Filters.eq(TARGETID, targetId)
        )).map(toComment::apply).into(comments);
        return comments;
    }

    @Override
    public List<Comment> findRatedByTarget(String targetType, String targetId) {
        List<Comment> comments = new ArrayList<>();

        getCollection().find(Filters.and(
                Filters.eq(TARGETTYPE, targetType),
                Filters.eq(TARGETID, targetId),
                Filters.exists(RATING, true)        // solo commenti con rating
        )).map(toComment::apply).into(comments);

        return comments;
    }

    @Override
    public void deleteById(String id) {
        DeleteResult result = getCollection().deleteOne(Filters.eq("_id", new ObjectId(id)));
        if (result.getDeletedCount() == 0) throw new RuntimeException("Comment not found with id: " + id);
    }
}