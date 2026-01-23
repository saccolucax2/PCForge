package it.unisannio.forum.persistence.mapper;

import it.unisannio.forum.model.Comment;


import static it.unisannio.forum.persistence.CommentRepository.*;
import org.bson.Document;

import java.util.function.Function;

public class CommentToDocumentMapper implements Function<Comment, Document> {


    @Override
    public Document apply(Comment comment) {
        return new Document(AUTHOR, comment.getAuthorId())
                .append(TARGETTYPE, comment.getTargetType())
                .append(TARGETID, comment.getTargetId())
                .append(CONTENT, comment.getContent())
                .append(PARENT, comment.getParentCommentId())
                .append(RATING, comment.getRating())
                .append(CREATEDAT, comment.getCreatedAt())
                .append(UPDATEAT, comment.getUpdatedAt());
    }
}
