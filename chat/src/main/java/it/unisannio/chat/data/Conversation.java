package it.unisannio.chat.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Conversation {
    private String id;
    private List<String> participants; // [userA, userB]
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    // 👇 Mappa con ultimo messaggio letto per utente
    private Map<String, LocalDateTime> lastReadAtPerUser;

    public Conversation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Map<String, LocalDateTime> getLastReadAtPerUser() { return lastReadAtPerUser; }
    public void setLastReadAtPerUser(Map<String, LocalDateTime> lastReadAtPerUser) {
        this.lastReadAtPerUser = lastReadAtPerUser;
    }
}