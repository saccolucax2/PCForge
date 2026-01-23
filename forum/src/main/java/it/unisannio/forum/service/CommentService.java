package it.unisannio.forum.service;

import it.unisannio.forum.model.Comment;
import it.unisannio.forum.persistence.CommentRepository;
import it.unisannio.forum.persistence.CommentRepositoryMongo;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository=CommentRepositoryMongo.getInstance();

    // Crea un nuovo commento
    public Comment create(Comment comment) {
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        return commentRepository.save(comment);
    }

    // Risposta a un commento esistente (threading)
    public Comment replyToComment(String parentId, Comment reply) {
        Optional<Comment> parent = commentRepository.findById(parentId);
        if (parent.isEmpty()) {
            throw new RuntimeException("Parent comment not found");
        }
        reply.setParentCommentId(parentId);
        reply.setCreatedAt(Instant.now());
        reply.setUpdatedAt(Instant.now());
        return commentRepository.save(reply);
    }

    // Recupera commenti per un target
    public List<Comment> findByTarget(String targetType, String targetId) {
        return commentRepository.findByTarget(targetType, targetId);
    }

    // Recupera un commento per id
    public Optional<Comment> findById(String id) {
        return commentRepository.findById(id);
    }

    // Elimina un commento
    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }
}