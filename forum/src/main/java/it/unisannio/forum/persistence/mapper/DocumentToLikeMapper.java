package it.unisannio.forum.persistence.mapper;

import it.unisannio.forum.model.Like;
import it.unisannio.forum.model.TargetType;
import org.bson.Document;
import java.util.function.Function;

public class DocumentToLikeMapper implements Function<Document, Like> {

    @Override
    public Like apply(Document doc) {
        Like like = new Like();

        Object id = doc.get("_id");
        if (id != null) {
            like.setId(id.toString());
        }

        like.setUserId(doc.getString("userId"));
        String tt = doc.getString("targetType");
        if(tt != null) like.setTargetType(TargetType.valueOf(tt));
        like.setTargetId(doc.getString("targetId"));

        if (doc.getDate("createdAt") != null) {
            like.setCreatedAt(doc.getDate("createdAt").toInstant());
        }

        return like;
    }
}
