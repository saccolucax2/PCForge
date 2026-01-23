package it.unisannio.buildgenerator.model;

import jakarta.persistence.*;

@Entity
public class Case extends Part {
    private String formFactor;
    private String bay;
    private String panelType; // e.g., "Tempered Glass", "Steel"
    private String ventola;

    @Override
    public String getModel() {
        return model; // Assuming model is inherited from Part
    }

    @Override
    public String getBrand() {
        return brand; // Assuming brand is inherited from Part
    }

    public String getPanelType() {
        return panelType;
    }

    public void setPanelType(String panelType) {
        this.panelType = panelType;
    }

    public String getVentola() {
        return ventola;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public void setVentola(String ventola) {
        this.ventola = ventola;
    }
    public void setFormFactor(String formFactor) {
        this.formFactor = formFactor;
    }

    public String getBay() {
        return bay;
    }

    public void setBay(String bay) {
        this.bay = bay;
    }

    @Override
    public float getPrice() {
        return price; // Example pricing logic
    }

    @Override
    public float getPerformance() {
        return calculateScore(); // Use the calculated score as performance
    }


    @Override
    public int compare(Part other) {
        if (!(other instanceof Case otherCase)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherCase.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio complessivo del Case secondo la formula.
     */
    private float calculateScore() {
        float formScore = evaluateFormFactor(this.formFactor);
        float bayScore = evaluateBay(this.bay);
        float panelScore = evaluatePanel(this.panelType);
        float fanScore = evaluateVentola(this.ventola);

        // Case_Score = 0.3*Formato + 0.15*Bay + 0.25*Pannelli + 0.30*Ventole
        return (0.3f * formScore)
                + (0.15f * bayScore)
                + (0.25f * panelScore)
                + (0.3f * fanScore);
    }

    /**
     * Valuta il formato del case.
     * ATX > Micro-ATX > Mini-ITX > altri
     */
    private float evaluateFormFactor(String form) {
        if (form == null) return 0;
        form = form.toUpperCase();
        if (form.contains("ATX") && !form.contains("MICRO") && !form.contains("MINI")) return 10;
        if (form.contains("MICRO-ATX")) return 8;
        if (form.contains("MINI-ITX")) return 6;
        return 5;
    }

    /**
     * Valuta il numero e tipo di bay (slot 2.5"/3.5").
     */
    private float evaluateBay(String bay) {
        if (bay == null) return 0;
        String normalized = bay.replaceAll("[^0-9]", " ").trim();
        try {
            int total = 0;
            for (String num : normalized.split("\\s+")) {
                if (!num.isEmpty()) total += Integer.parseInt(num);
            }
            return Math.min(total, 10); // Normalizzazione su 10
        } catch (Exception e) {
            return 5;
        }
    }

    /**
     * Valuta il tipo di pannello.
     * Tempered Glass > Steel > Plastic > altri
     */
    private float evaluatePanel(String panel) {
        if (panel == null) return 0;
        panel = panel.toUpperCase();
        if (panel.contains("TEMPERED")) return 10;
        if (panel.contains("GLASS")) return 9;
        if (panel.contains("STEEL")) return 8;
        if (panel.contains("ALUMINUM")) return 7;
        if (panel.contains("PLASTIC")) return 5;
        return 6;
    }

    /**
     * Valuta la qualità o quantità delle ventole.
     */
    private float evaluateVentola(String ventola) {
        if (ventola == null) return 0;
        ventola = ventola.toUpperCase();

        // Riconosce numeri (es. "3x120mm" o "2 ventole")
        try {
            int num = 0;
            for (String numStr : ventola.split("[^0-9]")) {
                if (!numStr.isEmpty()) num += Integer.parseInt(numStr);
            }
            return Math.min(num * 2f, 10f); // fino a 5 ventole = punteggio 10
        } catch (Exception e) {
            // fallback in base al tipo di ventola
            if (ventola.contains("ARGB")) return 10;
            if (ventola.contains("RGB")) return 9;
            if (ventola.contains("PWM")) return 8;
            return 6;
        }
    }

    /**
     * Confronta due Case e mostra le motivazioni.
     */
    public String compareWithReason(Case other) {
        float thisScore = calculateScore();
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

        compareField(reason, "Format", this.formFactor, other.formFactor,
                evaluateFormFactor(this.formFactor), evaluateFormFactor(other.formFactor));
        compareField(reason, "Bay", this.bay, other.bay,
                evaluateBay(this.bay), evaluateBay(other.bay));
        compareField(reason, "Panel", this.panelType, other.panelType,
                evaluatePanel(this.panelType), evaluatePanel(other.panelType));
        compareField(reason, "Fan", this.ventola, other.ventola,
                evaluateVentola(this.ventola), evaluateVentola(other.ventola));

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }

    /**
     * Supporto per rendere leggibile il confronto.
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
        return model + " - " + formFactor + " - " + bay + " - " + panelType + " - Fan: " + ventola;
    }
}