package it.unisannio.buildgenerator.model;

import jakarta.persistence.Entity;

@Entity
public class GPU extends Part{
    private String memory;
    private int CUDACores;
    private int memoryBus;
    private int baseClock;
    private int boostClock;
    private int TDP;

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
        if (!(other instanceof GPU otherGpu)) return 0;

        float thisScore = calculateScore();
        float otherScore = otherGpu.calculateScore();

        return Float.compare(thisScore, otherScore);
    }

    /**
     * Calcola il punteggio della GPU in base alla formula ponderata.
     */
    private float calculateScore() {
        float memGB = parseMemoryGB(this.memory);
        float tdpNorm = this.TDP / 100f; // normalizzato su 100W

        return (0.25f * this.CUDACores)
                + (0.20f * memGB)
                + (0.15f * this.memoryBus)
                + (0.10f * this.baseClock)
                + (0.15f * this.boostClock)
                - (0.15f * tdpNorm);
    }

    /**
     * Estrae la dimensione numerica della memoria in GB (es. "12GB" → 12).
     */
    private float parseMemoryGB(String memory) {
        if (memory == null) return 0;
        try {
            return Float.parseFloat(memory.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Confronta due GPU e spiega perché una è migliore dell'altra.
     */
    public String compareWithReason(GPU other) {
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
        if (this.CUDACores != other.CUDACores)
            reason.append("- Cores: ").append(Math.max(this.CUDACores, other.CUDACores))
                    .append(" vs ").append(Math.min(this.CUDACores, other.CUDACores))
                    .append(" → higher raw computing power.\n");

        float thisMem = parseMemoryGB(this.memory);
        float otherMem = parseMemoryGB(other.memory);
        if (thisMem != otherMem)
            reason.append("- VRAM: ").append(Math.max(thisMem, otherMem))
                    .append("GB vs ").append(Math.min(thisMem, otherMem))
                    .append("GB → better texture/render management.\n");

        if (this.memoryBus != other.memoryBus)
            reason.append("- Bus memoria: ").append(Math.max(this.memoryBus, other.memoryBus))
                    .append(" bit vs ").append(Math.min(this.memoryBus, other.memoryBus))
                    .append(" bit → higher band width.\n");

        if (this.baseClock != other.baseClock)
            reason.append("- Base Clock: ").append(Math.max(this.baseClock, other.baseClock))
                    .append(" MHz vs ").append(Math.min(this.baseClock, other.baseClock))
                    .append(" MHz → higher linear performance.\n");

        if (this.boostClock != other.boostClock)
            reason.append("- Boost Clock: ").append(Math.max(this.boostClock, other.boostClock))
                    .append(" MHz vs ").append(Math.min(this.boostClock, other.boostClock))
                    .append(" MHz → higher performance peak.\n");

        if (this.TDP != other.TDP)
            reason.append("- TDP: ").append(Math.min(this.TDP, other.TDP))
                    .append("W vs ").append(Math.max(this.TDP, other.TDP))
                    .append("W → better energy efficiency.\n");

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
        return model + " - " + CUDACores + " CUDA Cores, " + memory + " memory, Base Clock: " + baseClock + "MHz, Boost Clock: " + boostClock + "MHz";
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public int getTDP() {
        return TDP;
    }

    public void setTDP(int TDP) {
        this.TDP = TDP;
    }

    public int getBoostClock() {
        return boostClock;
    }

    public void setBoostClock(int boostClock) {
        this.boostClock = boostClock;
    }

    public int getBaseClock() {
        return baseClock;
    }

    public void setBaseClock(int baseClock) {
        this.baseClock = baseClock;
    }

    public int getMemoryBus() {
        return memoryBus;
    }

    public void setMemoryBus(int memoryBus) {
        this.memoryBus = memoryBus;
    }

    public int getCUDACores() {
        return CUDACores;
    }

    public void setCUDACores(int CUDACores) {
        this.CUDACores = CUDACores;
    }
}
