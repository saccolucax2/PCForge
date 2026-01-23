package it.unisannio.forum.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.DeleteResult;
import it.unisannio.forum.model.Like;
import it.unisannio.forum.persistence.mapper.LikeToDocumentMapper;
import it.unisannio.forum.persistence.mapper.DocumentToLikeMapper;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bson.types.ObjectId;
import java.util.Optional;

public class LikeRepositoryMongo implements LikeRepository {

    private final MongoClient mongoClient;
    private final Function<Like, Document> toDocument = new LikeToDocumentMapper();
    private final Function<Document, Like> toLike = new DocumentToLikeMapper();

    private static LikeRepositoryMongo INSTANCE;

    private LikeRepositoryMongo() {
        String host = System.getenv("MONGO_ADDRESS") != null ? System.getenv("MONGO_ADDRESS") : "localhost";
        String port = System.getenv("MONGO_PORT") != null ? System.getenv("MONGO_PORT") : "27017";
        mongoClient = MongoClients.create("mongodb://" + host + ":" + port);
    }

    public static LikeRepositoryMongo getInstance() {
        if (INSTANCE == null) INSTANCE = new LikeRepositoryMongo();
        return INSTANCE;
    }

    private MongoCollection<Document> getCollection() {
        return mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
    }

    @Override
    public Like save(Like like) {
        Document doc = toDocument.apply(like);
        InsertOneResult result = getCollection().insertOne(doc);
        if (!result.wasAcknowledged()) throw new RuntimeException("Failed to insert like");

        like.setId(doc.getObjectId("_id").toHexString());
        return like;
    }

    @Override
    public Optional<Like> findById(String id) {
        Document doc = getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(doc).map(toLike);
    }

    @Override
    public List<Like> findByTarget(String targetType, String targetId) {
        List<Like> likes = new ArrayList<>();
        getCollection().find(Filters.and(
                Filters.eq(TARGET_TYPE, targetType),
                Filters.eq(TARGET_ID, targetId)
        )).map(toLike::apply).into(likes);
        return likes;
    }

    @Override
    public void deleteById(String id) {
        DeleteResult result = getCollection().deleteOne(Filters.eq("_id", new ObjectId(id)));
        if (result.getDeletedCount() == 0) throw new RuntimeException("Like not found with id: " + id);
    }
}