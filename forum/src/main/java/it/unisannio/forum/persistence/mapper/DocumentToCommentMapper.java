package it.unisannio.forum.persistence.mapper;

import java.util.function.Function;

import org.bson.Document;

import it.unisannio.forum.model.Comment;

public class DocumentToCommentMapper implements Function<Document, Comment> {

    @Override
    public Comment apply(Document doc) {
        Comment comment = new Comment();

        // Gestione id: se esiste lo convertiamo a stringa
        Object id = doc.get("_id");
        if (id != null) {
            comment.setId(id.toString());
        }

        comment.setAuthorId(doc.getString("authorId"));
        comment.setTargetType(doc.getString("targetType"));
        comment.setTargetId(doc.getString("targetId"));
        comment.setContent(doc.getString("content"));
        comment.setParentCommentId(doc.getString("parentCommentId"));
        comment.setRating(doc.getInteger("rating"));

        // Per gli Instant: Mongo salva come Date, quindi li recuperiamo come `java.util.Date` e convertiamo
        if (doc.getDate("createdAt") != null) {
            comment.setCreatedAt(doc.getDate("createdAt").toInstant());
        }
        if (doc.getDate("updatedAt") != null) {
            comment.setUpdatedAt(doc.getDate("updatedAt").toInstant());
        }

        return comment;
    }
}
