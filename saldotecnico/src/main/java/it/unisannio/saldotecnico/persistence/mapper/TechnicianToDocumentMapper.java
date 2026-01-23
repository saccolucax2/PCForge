package it.unisannio.saldotecnico.persistence.mapper;

import it.unisannio.saldotecnico.model.PointTransaction;
import it.unisannio.saldotecnico.model.TechnicianProfile;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static it.unisannio.saldotecnico.persistence.TechnicianRepository.*;

public class TechnicianToDocumentMapper implements Function<TechnicianProfile, Document> {
    @Override
    public Document apply(TechnicianProfile profile) {
        Document doc = new Document();
        doc.put(USERID, profile.getUserId());
        doc.put(BIO, profile.getBio());
        doc.put(PHOTOURL, profile.getPhotoUrl());
        doc.put(CERTIFICATIONS, profile.getCertifications());
        doc.put(SKILLS, profile.getSkills());
        doc.put(POINTBALANCE, profile.getPointsBalance());

        List<Document> txDocs = new ArrayList<>();
        if(profile.getTransactions() != null) {
            for(PointTransaction t : profile.getTransactions()) {
                Document d = new Document();
                d.put("amount", t.getAmount());
                d.put("type", t.getType());
                d.put("reason", t.getReason());
                d.put("createdAt", t.getCreatedAt());
                txDocs.add(d);
            }
        }
        doc.put("transactions", txDocs);
        return doc;
    }
}
