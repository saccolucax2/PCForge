package it.unisannio.forum.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.DeleteResult;
import it.unisannio.forum.model.Post;
import it.unisannio.forum.persistence.mapper.PostToDocumentMapper;
import it.unisannio.forum.persistence.mapper.DocumentToPostMapper;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PostRepositoryMongo implements PostRepository {

    private final MongoClient mongoClient;
    private final Function<Post, Document> toDocument = new PostToDocumentMapper();
    private final Function<Document, Post> toPost = new DocumentToPostMapper();

    private static PostRepositoryMongo INSTANCE;

    private PostRepositoryMongo() {
        String host = System.getenv("MONGO_ADDRESS") != null ? System.getenv("MONGO_ADDRESS") : "localhost";
        String port = System.getenv("MONGO_PORT") != null ? System.getenv("MONGO_PORT") : "27017";
        mongoClient = MongoClients.create("mongodb://" + host + ":" + port);
    }

    public static PostRepositoryMongo getInstance() {
        if (INSTANCE == null) INSTANCE = new PostRepositoryMongo();
        return INSTANCE;
    }

    private MongoCollection<Document> getCollection() {
        return mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
    }

    public Post save(Post post) {
        Document doc = toDocument.apply(post);
        InsertOneResult result = getCollection().insertOne(doc);
        if (!result.wasAcknowledged()) {
            throw new RuntimeException("Failed to insert post");
        }
        post.setId(doc.get("_id").toString());
        return post;
    }

    public Optional<Post> findById(String id) {
        Document doc = getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(doc).map(toPost);
    }

    public List<Post> findAll() {
        List<Post> posts = new ArrayList<>();
        FindIterable<Document> docs = getCollection().find();
        docs.map(toPost::apply).into(posts);
        return posts;
    }

    public List<Post> findByAuthor(String authorId) {
        List<Post> posts = new ArrayList<>();
        FindIterable<Document> docs = getCollection().find(Filters.eq("authorId", authorId));
        docs.map(toPost::apply).into(posts);
        return posts;
    }

    public void deleteById(String id) {
        DeleteResult result = getCollection().deleteOne(Filters.eq("_id", new ObjectId(id)));
        if (result.getDeletedCount() == 0) {
            throw new RuntimeException("Post not found for id: " + id);
        }
    }
}