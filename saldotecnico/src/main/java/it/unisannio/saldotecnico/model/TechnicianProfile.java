package it.unisannio.saldotecnico.model;

import java.util.List;

public class TechnicianProfile {
    private String userId;                // ID dal User Service
    private String bio;
    private String photoUrl;
    private List<String> certifications;
    private List<String> skills;
    private int pointsBalance;            // saldo punti
    private List<PointTransaction> transactions;

    // Costruttore
    public TechnicianProfile() {}

    // Getter e Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public int getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(int pointsBalance) { this.pointsBalance = pointsBalance; }

    public List<PointTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<PointTransaction> transactions) { this.transactions = transactions; }
}