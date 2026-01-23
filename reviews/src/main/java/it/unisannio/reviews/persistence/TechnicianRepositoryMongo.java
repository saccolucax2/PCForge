package it.unisannio.reviews.persistence;

import com.mongodb.client.model.ReplaceOptions;
import it.unisannio.reviews.exception.TechnicianNotFoundException;
import it.unisannio.reviews.model.PointTransaction;
import it.unisannio.reviews.model.TechnicianProfile;
import it.unisannio.reviews.persistence.mapper.*;
import com.mongodb.client.*;
import org.bson.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import static com.mongodb.client.model.Filters.eq;

public class TechnicianRepositoryMongo implements TechnicianRepository {

    private static TechnicianRepositoryMongo INSTANCE;

    private final MongoClient mongoClient;
    private final Function<TechnicianProfile, Document> profileToDocument = new TechnicianToDocumentMapper();
    private final Function<Document, TechnicianProfile> documentToProfile = new DocumentToTechnicianMapper();

    // Singleton
    private TechnicianRepositoryMongo() {
        String host = System.getenv("MONGO_ADDRESS") != null ? System.getenv("MONGO_ADDRESS") : "localhost";
        String port = System.getenv("MONGO_PORT") != null ? System.getenv("MONGO_PORT") : "27017";
        mongoClient = MongoClients.create("mongodb://" + host + ":" + port);
    }

    public static TechnicianRepositoryMongo getInstance() {
        if (INSTANCE == null) INSTANCE = new TechnicianRepositoryMongo();
        return INSTANCE;
    }

    private MongoCollection<Document> getCollection() {
        return mongoClient.getDatabase(DB).getCollection(COLLECTION);
    }

    @Override
    public TechnicianProfile createOrUpdateProfile(TechnicianProfile profile) {
        Document doc = profileToDocument.apply(profile);
        getCollection().replaceOne(eq("userId", profile.getUserId()), doc, new ReplaceOptions().upsert(true));
        return findByUserId(profile.getUserId());
    }

    @Override
    public TechnicianProfile findByUserId(String userId) {
        Document doc = getCollection().find(eq("userId", userId)).first();
        if (doc == null) throw new TechnicianNotFoundException("Technician with userId " + userId + " not found");
        return documentToProfile.apply(doc);
    }

    @Override
    public List<TechnicianProfile> findAll() {
        List<TechnicianProfile> list = new ArrayList<>();
        getCollection().find().forEach(doc -> list.add(documentToProfile.apply(doc)));
        return list;
    }

    @Override
    public TechnicianProfile addPoints(String userId, int amount, String reason) {
        TechnicianProfile profile = findByUserId(userId);

        // Aggiorna il saldo
        profile.setPointsBalance(profile.getPointsBalance() + amount);

        // Aggiungi transazione
        if (profile.getTransactions() == null) profile.setTransactions(new ArrayList<>());
        PointTransaction tx = new PointTransaction();
        tx.setAmount(amount);
        tx.setType("accrual");
        tx.setReason(reason);
        tx.setCreatedAt(Instant.now());
        profile.getTransactions().add(tx);

        return createOrUpdateProfile(profile);
    }

    @Override
    public TechnicianProfile spendPoints(String userId, int amount, String reason) {
        TechnicianProfile profile = findByUserId(userId);
        if (profile.getPointsBalance() < amount)
            throw new IllegalArgumentException("Insufficient points");

        // Aggiorna il saldo
        profile.setPointsBalance(profile.getPointsBalance() - amount);

        // Aggiungi transazione
        if (profile.getTransactions() == null) profile.setTransactions(new ArrayList<>());
        PointTransaction tx = new PointTransaction();
        tx.setAmount(-amount);
        tx.setType("spend");
        tx.setReason(reason);
        tx.setCreatedAt(Instant.now());
        profile.getTransactions().add(tx);

        return createOrUpdateProfile(profile);
    }

    @Override
    public void deleteByUserId(String userId) {
        long deletedCount = getCollection().deleteOne(eq("userId", userId)).getDeletedCount();
    }
}