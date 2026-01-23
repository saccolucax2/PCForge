package it.unisannio.forum.persistence.mapper;

import it.unisannio.forum.model.Post;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.function.Function;

public class DocumentToPostMapper implements Function<Document, Post> {

    @Override
    public Post apply(Document doc) {
        Post post = new Post();


        ObjectId id = doc.getObjectId("_id");
        if (id != null) {
            post.setId(id.toHexString());
        }

        post.setAuthorId(doc.getString("authorId"));
        post.setTitle(doc.getString("title"));
        post.setContent(doc.getString("content"));

        if (doc.getDate("createdAt") != null) {
            post.setCreatedAt(doc.getDate("createdAt").toInstant());
        }
        if (doc.getDate("updatedAt") != null) {
            post.setUpdatedAt(doc.getDate("updatedAt").toInstant());
        }

        return post;
    }
}

