package it.unisannio.reviews.model;

import java.time.Instant;

public class PointTransaction {
    private int amount;          // positivo = accrual, negativo = spend
    private String type;         // "accrual" | "spend"
    private String reason;
    private Instant createdAt;

    // Costruttore vuoto
    public PointTransaction() {}

    // Costruttore completo
    public PointTransaction(int amount, String type, String reason, Instant createdAt) {
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    // Getter e Setter
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}