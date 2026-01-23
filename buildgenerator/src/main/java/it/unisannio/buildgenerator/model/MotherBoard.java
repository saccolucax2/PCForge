package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;
import java.util.List;

@Entity
public class MotherBoard extends Part{
    private String Chipset;
    private String Socket;
    private String RAMSupport;
    private List<String> PCIeSlots;
    private String SlotM2;

    @Override
    public float getPrice() {
        // Example pricing logic based on chipset and features
        return super.price; // Placeholder value
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getBrand() {
        return brand; // Assuming brand is inherited from Part
    }

    @Override
    public float getPerformance() {
        // Example performance logic based on chipset and features
        return calculateScore(); // Placeholder value
    }

    @Override
    public int compare(Part other) {
        if (!(other instanceof MotherBoard otherMb)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherMb.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio complessivo della scheda madre in base ai parametri.
     */
    private float calculateScore() {
        float chipsetScore = evaluateChipset(Chipset);
        float socketScore = evaluateSocket(Socket);
        float ramScore = evaluateRAM(RAMSupport);
        float pcieScore = evaluatePCIe(PCIeSlots);
        float m2Score = evaluateM2(SlotM2);

        return (0.25f * chipsetScore)
                + (0.25f * socketScore)
                + (0.20f * ramScore)
                + (0.15f * pcieScore)
                + (0.15f * m2Score);
    }

    /**
     * Confronta due schede madri e spiega perché una è migliore dell’altra.
     */
    public String compareWithReason(MotherBoard other) {
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

        // Analisi parametri principali
        compareField(reason, "Chipset", this.Chipset, other.Chipset, evaluateChipset(this.Chipset), evaluateChipset(other.Chipset));
        compareField(reason, "Socket", this.Socket, other.Socket, evaluateSocket(this.Socket), evaluateSocket(other.Socket));
        compareField(reason, "RAM Support", this.RAMSupport, other.RAMSupport, evaluateRAM(this.RAMSupport), evaluateRAM(other.RAMSupport));
        compareField(reason, "PCIe Slots", String.valueOf(this.PCIeSlots), String.valueOf(other.PCIeSlots), evaluatePCIe(this.PCIeSlots), evaluatePCIe(other.PCIeSlots));
        compareField(reason, "M.2 Slot", this.SlotM2, other.SlotM2, evaluateM2(this.SlotM2), evaluateM2(other.SlotM2));

        reason.append("\nTotal points:\n")
                .append(this.model).append(": ").append(String.format("%.2f", thisScore)).append("\n")
                .append(other.model).append(": ").append(String.format("%.2f", otherScore)).append("\n");

        return reason.toString();
    }

    /**
     * Aggiunge una riga di confronto solo se i valori differiscono.
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

    /* ========== Metodi di valutazione dei singoli parametri ========== */

    /**
     * Valuta il chipset in base alla generazione (più recente = punteggio più alto).
     */
    private float evaluateChipset(String chipset) {
        if (chipset == null) return 0;
        chipset = chipset.toUpperCase();
        if (chipset.contains("Z")) return 10;
        if (chipset.contains("B")) return 8;
        if (chipset.contains("H")) return 6;
        return 5; // default
    }

    /**
     * Valuta il socket: punteggio basato sulla compatibilità più recente.
     */
    private float evaluateSocket(String socket) {
        if (socket == null) return 0;
        socket = socket.toUpperCase();
        if (socket.contains("LGA1700") || socket.contains("AM5")) return 10;
        if (socket.contains("LGA1200") || socket.contains("AM4")) return 8;
        return 6; // generazione precedente
    }

    /**
     * Valuta il supporto RAM (DDR4/DDR5 e frequenze).
     */
    private float evaluateRAM(String ramSupport) {
        if (ramSupport == null) return 0;
        ramSupport = ramSupport.toUpperCase();
        if (ramSupport.contains("DDR5")) return 10;
        if (ramSupport.contains("DDR4")) return 8;
        return 6;
    }

    /**
     * Valuta il numero e tipo di slot PCIe.
     */
    private float evaluatePCIe(List<String> slots) {
        if (slots == null || slots.isEmpty()) return 0;
        float score = 0;
        for (String slot : slots) {
            if (slot == null) continue;
            String s = slot.toUpperCase();
            if (s.contains("5.0")) score += 5;
            else if (s.contains("4.0")) score += 4;
            else if (s.contains("3.0")) score += 3;
        }
        return Math.min(score, 10); // limite massimo
    }

    /**
     * Valuta la presenza e tipo di slot M.2.
     */
    private float evaluateM2(String slotM2) {
        if (slotM2 == null) return 0;
        slotM2 = slotM2.toUpperCase();
        if (slotM2.contains("PCIE 5.0")) return 10;
        if (slotM2.contains("PCIE 4.0")) return 8;
        if (slotM2.contains("PCIE 3.0")) return 6;
        if (slotM2.contains("YES") || slotM2.contains("SI")) return 5;
        return 0;
    }

    @Override
    public int comparePrice(Part other) {
        return Float.compare(this.getPrice(), other.getPrice());
    }

    @Override
    public String displayInfo() {
        return "Motherboard: " + model + " - Chipset: " + Chipset + ", Socket: " + Socket +
               ", RAM Support: " + RAMSupport + ", PCIe Slots: " + PCIeSlots + ", M.2 Slot: " + SlotM2;
    }

    public String getChipset() {
        return Chipset;
    }

    public void setChipset(String chipset) {
        Chipset = chipset;
    }

    public String getSlotM2() {
        return SlotM2;
    }

    public void setSlotM2(String slotM2) {
        SlotM2 = slotM2;
    }

    public List<String> getPCIeSlots(){
        return PCIeSlots;
    }
    public void setPCIeSlots(List<String> PCIeSlots) {
        this.PCIeSlots = PCIeSlots;
    }

    public String getRAMSupport() {
        return RAMSupport;
    }

    public void setRAMSupport(String RAMSupport) {
        this.RAMSupport = RAMSupport;
    }

    public String getSocket() {
        return Socket;
    }

    public void setSocket(String socket) {
        Socket = socket;
    }
}