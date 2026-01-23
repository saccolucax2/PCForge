package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;

@Entity
public class SSD extends Part {
    private String formFactor; // e.g., "2.5-inch", "M.2"
    private String interfaceType; // e.g., "SATA", "NVMe"
    private String capacity; // in GB
    private int readSpeed; // in MB/s
    private int writeSpeed; // in MB/s

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getBrand() {
        return brand;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public void setFormFactor(String formFactor) {
        this.formFactor = formFactor;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public int getReadSpeed() {
        return readSpeed;
    }

    public void setReadSpeed(int readSpeed) {
        this.readSpeed = readSpeed;
    }

    public int getWriteSpeed() {
        return writeSpeed;
    }

    public void setWriteSpeed(int writeSpeed) {
        this.writeSpeed = writeSpeed;
    }

    @Override
    public float getPrice() {
        return super.price;
    }

    @Override
    public float getPerformance() {
        return calculateScore(); // Example performance calculation
    }

    @Override
    public int compare(Part other) {
        if (!(other instanceof SSD otherSSD)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherSSD.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio complessivo della SSD secondo la formula.
     */
    private float calculateScore() {
        float interfaceScore = evaluateInterface(this.interfaceType);
        float formScore = evaluateFormFactor(this.formFactor);
        float capacityScore = parseCapacity(this.capacity);
        float readScore = this.readSpeed / 1000f;   // normalizzato
        float writeScore = this.writeSpeed / 1000f; // normalizzato

        // SSD_Score = 0.25*Interf + 0.20*Cap + 0.25*Read + 0.25*Write + 0.05*Form
        return (0.25f * interfaceScore)
                + (0.20f * capacityScore)
                + (0.25f * readScore)
                + (0.25f * writeScore)
                + (0.05f * formScore);
    }

    /**
     * Valuta il tipo di interfaccia.
     * NVMe > PCIe > SATA > IDE
     */
    private float evaluateInterface(String interf) {
        if (interf == null) return 0;
        interf = interf.toUpperCase();
        if (interf.contains("NVME")) return 10;
        if (interf.contains("PCIE")) return 9;
        if (interf.contains("SATA")) return 7;
        if (interf.contains("IDE")) return 5;
        return 6;
    }

    /**
     * Valuta il fattore di forma.
     * M.2 > 2.5-inch > PCIe > altri
     */
    private float evaluateFormFactor(String form) {
        if (form == null) return 0;
        form = form.toUpperCase();
        if (form.contains("M.2")) return 10;
        if (form.contains("2.5")) return 8;
        if (form.contains("PCI")) return 7;
        return 6;
    }

    /**
     * Interpreta la capacità espressa come stringa (es. "1TB" o "512GB").
     */
    private float parseCapacity(String cap) {
        if (cap == null) return 0;
        String normalized = cap.trim().toUpperCase();
        try {
            if (normalized.contains("TB")) {
                return Float.parseFloat(normalized.replaceAll("[^0-9.]", "")) * 1000;
            } else if (normalized.contains("GB")) {
                return Float.parseFloat(normalized.replaceAll("[^0-9.]", ""));
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    /**
     * Confronta due SSD e spiega perché una è migliore dell’altra.
     */
    public String compareWithReason(SSD other) {
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

        // Analisi dettagliata dei parametri
        compareField(reason, "Interface", this.interfaceType, other.interfaceType,
                evaluateInterface(this.interfaceType), evaluateInterface(other.interfaceType));
        compareField(reason, "Capacity", this.capacity, other.capacity,
                parseCapacity(this.capacity), parseCapacity(other.capacity));
        compareField(reason, "Read Speed", this.readSpeed + " MB/s", other.readSpeed + " MB/s",
                this.readSpeed, other.readSpeed);
        compareField(reason, "Write Speed", this.writeSpeed + " MB/s", other.writeSpeed + " MB/s",
                this.writeSpeed, other.writeSpeed);
        compareField(reason, "Form Factor", this.formFactor, other.formFactor,
                evaluateFormFactor(this.formFactor), evaluateFormFactor(other.formFactor));

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }

    /**
     * Metodo di supporto per creare righe di confronto leggibili.
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
        return "SSD{" +
                "model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", formFactor='" + formFactor + '\'' +
                ", interfaceType='" + interfaceType + '\'' +
                ", capacity='" + capacity + '\'' +
                ", readSpeed=" + readSpeed +
                ", writeSpeed=" + writeSpeed +
                '}';
    }
}