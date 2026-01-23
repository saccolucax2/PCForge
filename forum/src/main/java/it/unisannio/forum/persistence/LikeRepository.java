package it.unisannio.forum.persistence;

import it.unisannio.forum.model.Like;
import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    String USER_ID = "userId";
    String TARGET_TYPE = "targetType";
    String TARGET_ID = "targetId";
    String CREATED_AT = "createdAt";
    String DB_NAME = "forum";
    String COLLECTION_NAME = "likes";

    Like save(Like like);
    void deleteById(String id);
    List<Like> findByTarget(String targetType, String targetId);
    Optional<Like> findById(String id);

}