package it.unisannio.buildgenerator.model;

import jakarta.persistence.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PCBuild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Part> components = new ArrayList<>();

    public void addComponent(Part c) {
        components.add(c);
    }

    public List<Part> getComponents() {
        return components;
    }

    public void setComponents(List<Part> components) {
        this.components = components;
    }

    public float getTotalPrice() {
        return (float) components.stream().mapToDouble(Part::getPrice).sum();
    }

    public float getTotalPerformance() {
        return (float) components.stream().mapToDouble(Part::getPerformance).sum();
    }

    public String compareWithBuild(PCBuild other) {
        StringBuilder sb = new StringBuilder();

        sb.append("🔹 Comparator Results:\n");
        List<Part> thisComponents = this.getComponents();
        List<Part> otherComponents = other.getComponents();

        // Confronta componenti dello stesso tipo
        for (Part p1 : thisComponents) {
            Part p2 = otherComponents.stream()
                    .filter(p -> p.getClass().equals(p1.getClass()))
                    .findFirst()
                    .orElse(null);

            if (p2 != null) {
                try {
                    // Usa il metodo compareWithReason della classe specifica
                    Method method = p1.getClass().getMethod("compareWithReason", p1.getClass());
                    Object result = method.invoke(p1, p2);
                    sb.append("⚙️ ").append((p1.getClass().getSimpleName()).toUpperCase()).append(":")
                            .append(" ").append(p1.getModel())
                            .append(" vs ").append(p2.getModel())
                            .append(": ").append(result.toString()).append("\n");
                } catch (NoSuchMethodException e) {
                    // Fallback: confronta solo performance e prezzo
                    float perf1 = p1.getPerformance();
                    float perf2 = p2.getPerformance();

                    sb.append("⚙️ ").append((p1.getClass().getSimpleName()).toUpperCase()).append(":")
                            .append(" ").append(p1.getModel())
                            .append(" vs ").append(p2.getModel()).append(":\n");

                    if (perf1 > perf2) sb.append("👉 ").append(p1.getModel()).append(" is more efficient.\n");
                    else if (perf1 < perf2) sb.append("👉 ").append(p2.getModel()).append(" is more efficient.\n");
                    else sb.append("⚖️ Same performance.\n");

                    int priceCompare = Float.compare(p1.getPrice(), p2.getPrice());
                    if (priceCompare < 0) sb.append("💰 ").append(p1.getModel()).append(" is cheaper.\n");
                    else if (priceCompare > 0) sb.append("💰 ").append(p2.getModel()).append(" is cheaper.\n");
                } catch (Exception e) {
                    sb.append("⚙️ ").append((p1.getClass().getSimpleName()).toUpperCase()).append(":")
                            .append(" ").append(p1.getModel())
                            .append(": error comparing\n");
                }
            } else {
                sb.append("⚙️ ").append((p1.getClass().getSimpleName()).toUpperCase()).append(":")
                        .append(" ").append(p1.getModel())
                        .append(": not present in the other build\n");
            }
        }

        // Confronto prezzo totale
        float thisPrice = this.getTotalPrice();
        float otherPrice = other.getTotalPrice();
        sb.append("\n💰 Total price: ").append(String.format("%.2f",thisPrice))
                .append("€ vs ").append(String.format("%.2f",otherPrice)).append("€\n");

        // Confronto prestazioni totali
        float thisPerf = this.getTotalPerformance();
        float otherPerf = other.getTotalPerformance();
        sb.append("🚀 Total performance: ").append(String.format("%.2f",thisPerf))
                .append(" vs ").append(String.format("%.2f",otherPerf)).append("\n");

        // Confronto rapporto prezzo/prestazioni
        float thisRatio = thisPerf / thisPrice;
        float otherRatio = otherPerf / otherPrice;
        sb.append("⚖️ Performance/Price Ratio: ")
                .append(String.format("%.2f", thisRatio))
                .append(" vs ")
                .append(String.format("%.2f", otherRatio))
                .append("\n");

        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}