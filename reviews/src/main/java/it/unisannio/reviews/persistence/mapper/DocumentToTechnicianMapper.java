package it.unisannio.reviews.persistence.mapper;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bson.Document;

import it.unisannio.reviews.model.PointTransaction;
import it.unisannio.reviews.model.TechnicianProfile;
import static it.unisannio.reviews.persistence.TechnicianRepository.BIO;
import static it.unisannio.reviews.persistence.TechnicianRepository.CERTIFICATIONS;
import static it.unisannio.reviews.persistence.TechnicianRepository.PHOTOURL;
import static it.unisannio.reviews.persistence.TechnicianRepository.POINTBALANCE;
import static it.unisannio.reviews.persistence.TechnicianRepository.SKILLS;
import static it.unisannio.reviews.persistence.TechnicianRepository.TRANSACTIONS;
import static it.unisannio.reviews.persistence.TechnicianRepository.USERID;

public class DocumentToTechnicianMapper implements Function<Document, TechnicianProfile> {
    @SuppressWarnings("unchecked")
    @Override
    public TechnicianProfile apply(Document doc) {
        TechnicianProfile profile = new TechnicianProfile();
        profile.setUserId(doc.getString(USERID));
        profile.setBio(doc.getString(BIO));
        profile.setPhotoUrl(doc.getString(PHOTOURL));
        profile.setCertifications((List<String>) doc.getOrDefault(CERTIFICATIONS, new ArrayList<>()));
        profile.setSkills((List<String>) doc.getOrDefault(SKILLS, new ArrayList<>()));
        profile.setPointsBalance(doc.getInteger(POINTBALANCE, 0));

        List<Document> txDocs = (List<Document>) doc.getOrDefault(TRANSACTIONS, new ArrayList<>());
        List<PointTransaction> txs = new ArrayList<>();
        for(Document d : txDocs){
            PointTransaction t = new PointTransaction();
            t.setAmount(d.getInteger("amount"));
            t.setType(d.getString("type"));
            t.setReason(d.getString("reason"));

            // Converte java.util.Date -> Instant
            Object dateObj = d.get("createdAt");
            switch (dateObj) {
                case java.util.Date date -> t.setCreatedAt(date.toInstant());
                case Instant instant -> t.setCreatedAt(instant);
                default -> {
                }
            }

            txs.add(t);
        }
        profile.setTransactions(txs);

        return profile;
    }
}
