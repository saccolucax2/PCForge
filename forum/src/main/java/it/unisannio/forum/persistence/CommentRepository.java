package it.unisannio.forum.persistence;

import it.unisannio.forum.model.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    String DB_NAME = "forum";
    String COLLECTION_NAME = "comments";
    String AUTHOR="authorId";
    String TARGETTYPE="targetType"; // BUILD, COMPONENT, POST, COMMENT, TECHNICIAN
    String TARGETID="targetId";
    String CONTENT="content";
    String PARENT="parentCommentId";
    String RATING="rating"; // solo se targetType = TECHNICIAN
    String CREATEDAT="createdAt";
    String UPDATEAT="updatedAt";

    Comment save(Comment comment);
    List<Comment> findByTarget(String targetType, String targetId);

    List<Comment> findRatedByTarget(String targetType, String targetId);

    Optional<Comment> findById(String id);
    void deleteById(String id);
}