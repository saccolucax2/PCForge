package it.unisannio.forum.persistence;

import it.unisannio.forum.model.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository{
    String AUTHOR_ID = "authorId";
    String TITLE = "title";
    String CONTENT = "content";
    String CREATED_AT = "createdAt";
    String UPDATED_AT = "updatedAt";
    String DB_NAME = "forum";
    String COLLECTION_NAME = "posts";

    Post save(Post post);
    Optional<Post> findById(String id);
    void deleteById(String id);
    List<Post> findAll();
    List<Post> findByAuthor(String authorId);

}