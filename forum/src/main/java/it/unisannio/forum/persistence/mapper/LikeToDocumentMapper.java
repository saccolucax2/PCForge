package it.unisannio.forum.persistence.mapper;

import it.unisannio.forum.model.Like;
import org.bson.Document;

import java.util.function.Function;

import static it.unisannio.forum.persistence.LikeRepository.*;

public class LikeToDocumentMapper implements Function<Like, Document> {



    @Override
    public Document apply(Like like) {
        return new Document(USER_ID, like.getUserId())
                .append(TARGET_TYPE, like.getTargetType() != null ? like.getTargetType().name() : null)
                .append(TARGET_ID, like.getTargetId())
                .append(CREATED_AT, like.getCreatedAt());
    }
}
