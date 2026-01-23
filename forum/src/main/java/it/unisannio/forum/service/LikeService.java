package it.unisannio.forum.service;

import it.unisannio.forum.model.Like;
import it.unisannio.forum.persistence.LikeRepository;
import it.unisannio.forum.persistence.LikeRepositoryMongo;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class LikeService {

    private final LikeRepository likeRepository=LikeRepositoryMongo.getInstance();

    // Lascia un like
    public Like create(Like like) {
        if (like.getTargetType() == null) {
            throw new IllegalArgumentException("targetType is required");
        }
        like.setCreatedAt(Instant.now());
        return likeRepository.save(like);
    }

    // Rimuove un like
    public void deleteById(String id) {
        likeRepository.deleteById(id);
    }

    // Lista dei like su un target (post, comment, technician)
    public List<Like> findByTarget(String targetType, String targetId) {
        return likeRepository.findByTarget(targetType, targetId);
    }
}