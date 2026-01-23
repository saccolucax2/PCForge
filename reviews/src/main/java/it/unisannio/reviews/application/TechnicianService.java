package it.unisannio.reviews.application;

import it.unisannio.reviews.exception.TechnicianNotFoundException;
import it.unisannio.reviews.model.PointTransaction;
import it.unisannio.reviews.model.TechnicianProfile;
import it.unisannio.reviews.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TechnicianService {

    private final TechnicianRepository repository;

    public TechnicianService() {
        this.repository = TechnicianRepositoryMongo.getInstance();
    }

    /**
     * Crea o aggiorna un profilo tecnico.
     */
    public TechnicianProfile createOrUpdateProfile(TechnicianProfile profile) {
        return repository.createOrUpdateProfile(profile);
    }

    /**
     * Recupera il profilo di un tecnico dato l'userId.
     */
    public TechnicianProfile getProfileByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public int getRatingPoints(String userId) {
        TechnicianProfile profile = repository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("Technician not found");
        }
        return profile.getPointsBalance();
    }

    public void addRatingPoints(String technicianId, int stars, String reason) {
        TechnicianProfile profile = repository.findByUserId(technicianId);
        if (profile == null) {
            throw new IllegalArgumentException("Technician not found");
        }

        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        int currentBalance = profile.getPointsBalance();
        int newBalance;

        if (currentBalance == 0) {
            // Primo voto
            newBalance = stars;
        } else {
            // Media con il punteggio attuale
            newBalance = (currentBalance + stars) / 2;
        }

        profile.setPointsBalance(newBalance);

        // Registra la transazione
        if (profile.getTransactions() == null) {
            profile.setTransactions(new ArrayList<>());
        }

        PointTransaction tx = new PointTransaction(
                stars,
                "rating",
                reason != null ? reason : "user_rating",
                Instant.now()
        );

        profile.getTransactions().add(tx);

        // Salva nel DB
        repository.createOrUpdateProfile(profile);
    }

    /**
     * Recupera tutti i profili tecnici.
     */
    public List<TechnicianProfile> getAllProfiles() {
        return repository.findAll();
    }

    /**
     * Aggiunge punti al tecnico.
     */
    public TechnicianProfile addPoints(String userId, int amount, String reason) {
        return repository.addPoints(userId, amount, reason);
    }

    /**
     * Deduce punti dal tecnico.
     */
    public TechnicianProfile spendPoints(String userId, int amount, String reason) {
        return repository.spendPoints(userId, amount, reason);
    }

    /**
     * Controllo se esiste un tecnico con userId.
     */
    public boolean existsByUserId(String userId) {
        try {
            repository.findByUserId(userId);
            return true;
        } catch (TechnicianNotFoundException e) {
            return false;
        }
    }

    public void deleteTechnicianProfile(String userId) {
        repository.deleteByUserId(userId);
    }

}