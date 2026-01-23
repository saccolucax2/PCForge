package it.unisannio.saldotecnico.persistence;

import it.unisannio.saldotecnico.model.TechnicianProfile;
import java.util.List;

public interface TechnicianRepository {
    String DB = "techniciandb";
    String USERID = "userId";
    String BIO = "bio";
    String PHOTOURL = "photoUrl";
    String CERTIFICATIONS = "certifications";
    String POINTBALANCE = "pointsBalance";
    String TRANSACTIONS = "transactions";
    String SKILLS="skills";
    String COLLECTION = "technicians";

    TechnicianProfile createOrUpdateProfile(TechnicianProfile profile);
    TechnicianProfile findByUserId(String userId);
    List<TechnicianProfile> findAll();
    TechnicianProfile addPoints(String userId, int amount, String reason);
    TechnicianProfile spendPoints(String userId, int amount, String reason);
    boolean deleteByUserId(String userId);

}