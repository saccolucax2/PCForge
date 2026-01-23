package it.unisannio.forum.persistence.mapper;

import it.unisannio.forum.model.Post;
import org.bson.Document;

import java.util.Date;
import java.util.function.Function;

import static it.unisannio.forum.persistence.PostRepository.*;

public class PostToDocumentMapper implements Function<Post, Document> {



    @Override
    public Document apply(Post post) {
        return new Document(AUTHOR_ID, post.getAuthorId())
                .append(TITLE, post.getTitle())
                .append(CONTENT, post.getContent())
                .append(CREATED_AT, post.getCreatedAt() != null ? Date.from(post.getCreatedAt()) : null)
                .append(UPDATED_AT, post.getUpdatedAt() != null ? Date.from(post.getUpdatedAt()) : null);

    }
}

