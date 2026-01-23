package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;

@Entity
public class Cooler extends Part{
    private String type;
    private String socketCompatibility;
    private String ventola;
    private String heatPipes;

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getBrand() {
        return brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSocketCompatibility() {
        return socketCompatibility;
    }
    public void setSocketCompatibility(String socketCompatibility) {
        this.socketCompatibility = socketCompatibility;
    }

    public String getVentola() {
        return ventola;
    }
    public void setVentola(String ventola) {
        this.ventola = ventola;
    }

    public String getHeatPipes() {
        return heatPipes;
    }

    public void setHeatPipes(String heatPipes) {
        this.heatPipes = heatPipes;
    }

    @Override
    public float getPrice() {
        return super.price;
    }

    @Override
    public float getPerformance() {
        return calculateScore();
    }

    @Override
    public int compare(Part other) {
        if (!(other instanceof Cooler otherCooler)) return 0;
        return Float.compare(this.calculateScore(), otherCooler.calculateScore());
    }

    /**
     * Calcola il punteggio complessivo del Cooler secondo la formula:
     * Cooler_Score = 0.25*Tipo + 0.25*Compat + 0.30*Ventola + 0.20*HeatPipes
     */
    private float calculateScore() {
        float typeScore = evaluateType(this.type);
        float compatScore = evaluateSocket(this.socketCompatibility);
        float ventolaScore = evaluateVentola(this.ventola);
        float heatPipeScore = evaluateHeatPipes(this.heatPipes);

        return (0.25f * typeScore)
                + (0.25f * compatScore)
                + (0.30f * ventolaScore)
                + (0.20f * heatPipeScore);
    }

    /** Valuta il tipo di raffreddamento: liquid > air */
    private float evaluateType(String type) {
        if (type == null) return 0;
        type = type.toUpperCase();
        if (type.contains("LIQUID") || type.contains("AIO") || type.contains("WATER")) return 10;
        if (type.contains("AIR")) return 7;
        return 5;
    }

    /** Valuta la compatibilità con i socket */
    private float evaluateSocket(String socket) {
        if (socket == null) return 0;
        // Più socket supportati = maggiore compatibilità
        int count = socket.split("[,;/ ]+").length;
        return Math.min(count * 2f, 10f);
    }

    /** Valuta la ventola (numero, dimensione o RPM) */
    private float evaluateVentola(String ventola) {
        if (ventola == null) return 0;
        ventola = ventola.toUpperCase();

        int size = 0;
        for (String num : ventola.split("[^0-9]")) {
            if (!num.isEmpty()) {
                int val = Integer.parseInt(num);
                if (val > size) size = val;
            }
        }

        if (ventola.contains("RPM")) {
            // Calcola un punteggio proporzionale alla velocità massima
            try {
                int rpm = Integer.parseInt(ventola.replaceAll("[^0-9]", ""));
                return Math.min(rpm / 300f, 10f);
            } catch (Exception e) {
                return 6f;
            }
        }

        // Valuta in base alla dimensione (es. 120mm, 140mm)
        if (size >= 140) return 9f;
        if (size >= 120) return 8f;
        if (size >= 90) return 7f;
        if (size >= 80) return 6f;

        return 5f;
    }

    /** Valuta il numero di heat pipes */
    private float evaluateHeatPipes(String heatPipes) {
        if (heatPipes == null) return 0;
        try {
            int num = Integer.parseInt(heatPipes.replaceAll("[^0-9]", ""));
            return Math.min(num * 2f, 10f);
        } catch (Exception e) {
            return 6f;
        }
    }

    /**
     * Confronta due Cooler e mostra una motivazione leggibile.
     */
    public String compareWithReason(Cooler other) {
        float thisScore = this.calculateScore();
        float otherScore = other.calculateScore();

        StringBuilder reason = new StringBuilder();
        reason.append("\n");

        if (thisScore > otherScore) {
            reason.append("👉 ").append(this.model).append(" is more efficient.\n");
        } else if (thisScore < otherScore) {
            reason.append("👉 ").append(other.model).append(" is more efficient.\n");
        } else {
            reason.append("⚖️ Same components.\n");
        }

        compareField(reason, "Type", this.type, other.type,
                evaluateType(this.type), evaluateType(other.type));
        compareField(reason, "Socket Compatibility", this.socketCompatibility, other.socketCompatibility,
                evaluateSocket(this.socketCompatibility), evaluateSocket(other.socketCompatibility));
        compareField(reason, "Fan", this.ventola, other.ventola,
                evaluateVentola(this.ventola), evaluateVentola(other.ventola));
        compareField(reason, "Heat Pipes", this.heatPipes, other.heatPipes,
                evaluateHeatPipes(this.heatPipes), evaluateHeatPipes(other.heatPipes));

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }

    /** Supporto per confronti leggibili */
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
        return "Cooler{" +
                "model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", type='" + type + '\'' +
                ", socketCompatibility='" + socketCompatibility + '\'' +
                ", ventola='" + ventola + '\'' +
                ", heatPipes='" + heatPipes + '\'' +
                '}';
    }

}