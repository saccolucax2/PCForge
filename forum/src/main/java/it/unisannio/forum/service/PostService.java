package it.unisannio.forum.service;

import it.unisannio.forum.model.Post;
import it.unisannio.forum.persistence.PostRepository;
import it.unisannio.forum.persistence.PostRepositoryMongo;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository= PostRepositoryMongo.getInstance();

    // Crea un post
    public Post create(Post post) {
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        return postRepository.save(post);
    }

    // Recupera post per id
    public Optional<Post> findById(String id) {
        return postRepository.findById(id);
    }

    // Recupera tutti i post
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public List<Post> findByUserId(String author) {
        return postRepository.findByAuthor(author);
    }

    // Elimina post
    public void deleteById(String id) {
        postRepository.deleteById(id);
    }
}