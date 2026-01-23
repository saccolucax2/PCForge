package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;
import java.util.List;

@Entity
public class PSU extends Part{ //alimentatore
    private int wattage;
    private String efficiencyRating; // e.g., "80 Plus Gold"
    private String ventola;
    private List<String> principalConnector;// e.g., "24-pin ATX"

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
        if (!(other instanceof PSU otherPSU)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherPSU.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio complessivo della PSU secondo la formula fornita.
     */
    private float calculateScore() {
        float wattScore = this.wattage / 100f; // normalizzato
        float effScore = evaluateEfficiency(this.efficiencyRating);
        float fanScore = evaluateFan(this.ventola);
        float connScore = (this.principalConnector != null) ? this.principalConnector.size() : 0;

        // PSU_Score = 0.3*Watt + 0.4*Eff + 0.15*Vent + 0.15*Conn
        return (0.3f * wattScore)
                + (0.4f * effScore)
                + (0.15f * fanScore)
                + (0.15f * connScore);
    }

    /**
     * Valuta la classe di efficienza (es. 80 Plus Gold, Platinum, ecc.)
     * Ritorna un punteggio normalizzato da 1 a 10.
     */
    private float evaluateEfficiency(String rating) {
        if (rating == null) return 0;
        rating = rating.toUpperCase();
        if (rating.contains("TITANIUM")) return 10;
        if (rating.contains("PLATINUM")) return 9;
        if (rating.contains("GOLD")) return 8;
        if (rating.contains("SILVER")) return 7;
        if (rating.contains("BRONZE")) return 6;
        if (rating.contains("80")) return 5;
        return 4; // non certificato
    }

    /**
     * Valuta la qualità della ventola (es. dimensione, rumorosità, tipo)
     */
    private float evaluateFan(String fan) {
        if (fan == null) return 0;
        fan = fan.toLowerCase();
        if (fan.contains("silent") || fan.contains("quiet")) return 9;
        if (fan.contains("140")) return 8;
        if (fan.contains("120")) return 7;
        if (fan.contains("80")) return 5;
        return 6; // default generico
    }

    /**
     * Fornisce un confronto dettagliato tra due PSU.
     */
    public String compareWithReason(PSU other) {
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

        // Analisi dettagliata
        compareField(reason, "Wattage", this.wattage + "W", other.wattage + "W", this.wattage, other.wattage);
        compareField(reason, "Efficiency", this.efficiencyRating, other.efficiencyRating,
                evaluateEfficiency(this.efficiencyRating), evaluateEfficiency(other.efficiencyRating));
        compareField(reason, "Fan", this.ventola, other.ventola,
                evaluateFan(this.ventola), evaluateFan(other.ventola));
        compareField(reason, "Connectors", String.valueOf(this.principalConnector != null ? this.principalConnector.size() : 0),
                String.valueOf(other.principalConnector != null ? other.principalConnector.size() : 0),
                (this.principalConnector != null ? this.principalConnector.size() : 0),
                (other.principalConnector != null ? other.principalConnector.size() : 0));

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }

    /**
     * Metodo di supporto per aggiungere una riga di confronto leggibile.
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
        return "PSU: " + model + " - Wattage: " + wattage + "W, Efficiency: " + efficiencyRating +
               ", Fan: " + ventola + ", Connectors: " + principalConnector;
    }
    public int getWattage() {
        return wattage;
    }
    public void setWattage(int wattage) {
        this.wattage = wattage;
    }
    public String getEfficiencyRating() {
        return efficiencyRating;
    }
    public void setEfficiencyRating(String efficiencyRating) {
        this.efficiencyRating = efficiencyRating;
    }
    public String getVentola() {
        return ventola;
    }
    public void setVentola(String ventola) {
        this.ventola = ventola;
    }
    public List<String> getPrincipalConnector() {
        return principalConnector;
    }

    public void setPrincipalConnector(List<String> principalConnector) {
        this.principalConnector = principalConnector;
    }
}