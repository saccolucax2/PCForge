package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;

@Entity
public class RAM extends Part{
    private String type; // e.g., "DDR4", "DDR5"
    private int capacity; // in GB
    private int frequency; // in MHz
    private String CASLatency; // e.g., "CL16"
    private float tension;

    @Override
    public String getModel() {
        return model; // Assuming model is inherited from Part
    }

    @Override
    public String getBrand() {
        return brand; // Assuming brand is inherited from Part
    }

    @Override
    public float getPrice() {
        return super.price; // Example pricing logic
    }

    @Override
    public float getPerformance() {
        return calculateScore(); // Example performance logic
    }

    @Override
    public int compare(Part other) {
        if (!(other instanceof RAM otherRam)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherRam.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio complessivo della RAM in base alla formula ponderata.
     */
    private float calculateScore() {
        // Tipo (DDR4/DDR5) → 0-10
        float typeScore = evaluateType(this.type);

        // Frequenza normalizzata: 1000 MHz → 1.0
        float freqScore = this.frequency / 1000f;

        // CAS Latency normalizzata: 10 → 1.0
        float casValue = parseCASLatency(this.CASLatency) / 10f;

        // Tensione normalizzata: 1V → 1.0
        float tensionScore = this.tension; // se tensione tipica 1-1.5V, scala ok

        // Capacità normalizzata: 16GB → 1.0
        float capacityScore = this.capacity / 16f; // adatta al tuo standard

        // Formula: RAM_Score = 0.2*Tipo + 0.35*Freq - 0.25*CAS - 0.10*Tensione + 0.10*Capacità
        return (0.20f * typeScore)
                + (0.35f * freqScore)
                - (0.25f * casValue)
                - (0.10f * tensionScore)
                + (0.10f * capacityScore);
    }

    /**
     * Valuta il tipo di RAM.
     * DDR5 = 10, DDR4 = 8, DDR3 = 6, altrimenti 5.
     */
    private float evaluateType(String type) {
        if (type == null) return 0;
        type = type.toUpperCase();
        if (type.contains("DDR5")) return 10;
        if (type.contains("DDR4")) return 8;
        if (type.contains("DDR3")) return 6;
        return 5;
    }

    /**
     * Estrae il primo valore numerico della CAS Latency (es. "CL16-18-18" → 16).
     */
    private float parseCASLatency(String cas) {
        if (cas == null) return 0;
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(cas);
            if (m.find()) {
                return Float.parseFloat(m.group());
            }
        } catch (Exception e) {
            // ignora e ritorna 0
        }
        return 0;
    }

    /**
     * Confronta due RAM e spiega perché una è migliore dell’altra.
     */
    public String compareWithReason(RAM other) {
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

        // Analisi parametri
        compareField(reason, "Type", this.type, other.type, evaluateType(this.type), evaluateType(other.type));
        compareField(reason, "Frequency", this.frequency + " MHz", other.frequency + " MHz", this.frequency, other.frequency);
        compareField(reason, "CAS Latency", this.CASLatency, other.CASLatency, -parseCASLatency(this.CASLatency), -parseCASLatency(other.CASLatency));
        compareField(reason, "Voltage", this.tension + "V", other.tension + "V", -this.tension, -other.tension);
        compareField(reason, "Capacity", this.capacity + "GB", other.capacity + "GB", this.capacity, other.capacity);

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }

    /**
     * Aggiunge una riga di confronto se i valori differiscono.
     */
    private void compareField(StringBuilder sb, String name, String val1, String val2, float score1, float score2) {
        if (score1 == score2) return;
        if (score1 > score2)
            sb.append("- ").append(name).append(": ").append(val1).append(" vs ").append(val2)
                    .append(" → better ").append(name.toLowerCase()).append(".\n");
        else
            sb.append("- ").append(name).append(": ").append(val2).append(" vs ").append(val1)
                    .append(" → better ").append(name.toLowerCase()).append(".\n");
    }

    @Override
    public int comparePrice(Part other) {
        return Float.compare(this.getPrice(), other.getPrice());
    }

    @Override
    public String displayInfo() {
        return model + " - " + capacity + "GB " + type + " @ " + frequency + "MHz, CAS Latency: " + CASLatency;
    }

    public String getCASLatency() {
        return CASLatency;
    }

    public void setCASLatency(String CASLatency) {
        this.CASLatency = CASLatency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public float getTension() {
        return tension;
    }

    public void setTension(float tension) {
        this.tension = tension;
    }
}