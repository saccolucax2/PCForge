package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;

@Entity
public class CPU extends Part {
    private int cores;
    private int threads;
    private String socketType;
    private String cacheSize; // e.g., "L3 16MB"
    private float freqBase;
    private float freqBoost;

    @Override
    public String getModel() {
        return model; // Assuming model is inherited from Part
    }

    public int getCores() {
        return cores;
    }

    public int getThreads() {
        return threads;
    }

    public String getSocketType() {
        return socketType;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public float getFreqBase() {
        return freqBase;
    }

    public float getFreqBoost() {
        return freqBoost;
    }

    @Override
    public String getBrand() {
        return brand; // Assuming brand is inherited from Part
    }

    @Override
    public float getPrice() {
        return super.price;
    }

    @Override
    public float getPerformance() {
        return  calculateScore();
    }

    public void setCores(int cores) {
        this.cores = cores;
    }
    public void setThreads(int threads) {
        this.threads = threads;
    }
    public void setSocketType(String socketType) {
        this.socketType = socketType;
    }
    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }
    public void setFreqBase(float freqBase) {
        this.freqBase = freqBase;
    }
    public void setFreqBoost(float freqBoost) {
        this.freqBoost = freqBoost;
    }

    @Override
    public int compare(Part other) {
        if (!(other instanceof CPU otherCpu)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherCpu.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio della CPU in base alla formula di performance.
     */
    private float calculateScore() {
        float cacheMB = parseCacheMB(this.cacheSize);
        float socket = parseSocketType(this.socketType);
        return (0.15f * cacheMB)
                + (0.10f * socket)
                + (0.25f * cores)
                + (0.15f * threads)
                + (0.15f * freqBase)
                + (0.20f * freqBoost);
    }

    /**
     * Assegna un punteggio alla socket in base alla sua tipologia (più moderna e/o performante).
     */
    private float parseSocketType(String socketType) {
        if (socketType == null) return 0;
        else return switch (socketType.toUpperCase()) {
            case "AM5" -> 10;
            case "LGA1700" -> 8;
            case "AM4" -> 6;
            case "LGA1200" -> 5;
            default -> 4;
        };
    }

    /**
     * Estrae la dimensione numerica della cache in MB (es. "L3 16MB" → 16).
     */
    private float parseCacheMB(String cacheSize) {
        if (cacheSize == null) return 0;
        try {
            return Float.parseFloat(cacheSize.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Confronta due CPU e spiega perché una è migliore.
     */
    public String compareWithReason(CPU other) {
        float thisScore = calculateScore();
        float otherScore = other.calculateScore();

        StringBuilder reason = new StringBuilder();
        reason.append("\n");
        if (thisScore > otherScore) {
            reason.append("👉 ").append(this.model).append(" is more efficient.\n");
        } else if (thisScore < otherScore) {
            reason.append("👉 ").append(other.model).append(" is more efficient.\n");
        } else {
            reason.append("⚖️ Same component.\n");
        }

        // Analisi dettagliata per parametro
        if (this.cores != other.cores)
            reason.append("- Core: ").append(Math.max(this.cores, other.cores))
                    .append(" vs ").append(Math.min(this.cores, other.cores))
                    .append(" → more usable core for multitasking.\n");

        if (this.threads != other.threads)
            reason.append("- Thread: ").append(Math.max(this.threads, other.threads))
                    .append(" vs ").append(Math.min(this.threads, other.threads))
                    .append(" → better parallelism.\n");

        if (this.freqBase != other.freqBase)
            reason.append("- Base Frequency: ").append(Math.max(this.freqBase, other.freqBase))
                    .append("GHz vs ").append(Math.min(this.freqBase, other.freqBase))
                    .append("GHz → higher linear speed.\n");

        if (this.freqBoost != other.freqBoost)
            reason.append("- Boost Frequency: ").append(Math.max(this.freqBoost, other.freqBoost))
                    .append("GHz vs ").append(Math.min(this.freqBoost, other.freqBoost))
                    .append("GHz → better performance under load.\n");

        float thisCache = parseCacheMB(this.cacheSize);
        float otherCache = parseCacheMB(other.cacheSize);
        if (thisCache != otherCache)
            reason.append("- L3 Cache: ").append(Math.max(thisCache, otherCache))
                    .append("MB vs ").append(Math.min(thisCache, otherCache))
                    .append("MB → better data management.\n");

        float thisSocket = parseSocketType(this.socketType);
        float otherSocket = parseSocketType(other.socketType);
        if (thisSocket != otherSocket)
            reason.append("- Socket: ").append(Math.max(thisSocket, otherSocket))
                    .append(" vs ").append(Math.min(thisSocket, otherSocket))
                    .append(" → newer and better socket type.\n");

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }


    @Override
    public int comparePrice(Part other) {
        return Float.compare(this.getPrice(), other.getPrice());
    }

    @Override
    public String displayInfo() {
        return model + " - " + cores + " cores @" + freqBase + "GHz (Boost: " + freqBoost + "GHz), " +
                threads + " threads, Socket: " + socketType + ", Cache: " + cacheSize;
    }

}