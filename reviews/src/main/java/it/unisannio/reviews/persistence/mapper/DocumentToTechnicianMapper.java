package it.unisannio.reviews.persistence.mapper;


import it.unisannio.reviews.model.PointTransaction;
import it.unisannio.reviews.model.TechnicianProfile;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static it.unisannio.reviews.persistence.TechnicianRepository.*;

public class DocumentToTechnicianMapper implements Function<Document, TechnicianProfile> {
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
            if(dateObj instanceof java.util.Date) {
                t.setCreatedAt(((java.util.Date) dateObj).toInstant());
            } else if(dateObj instanceof Instant) {
                t.setCreatedAt((Instant) dateObj);
            }

            txs.add(t);
        }
        profile.setTransactions(txs);

        return profile;
    }
}
