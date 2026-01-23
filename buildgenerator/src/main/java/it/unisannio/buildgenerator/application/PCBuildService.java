package it.unisannio.buildgenerator.application;

import it.unisannio.buildgenerator.model.*;
import it.unisannio.buildgenerator.persistence.*;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PCBuildService {
//COSTRUTTORE

    private final PCBuildRepository repository;

    public PCBuildService(PCBuildRepository repository) {
        this.repository = repository;
    }

    public PCBuild createBuild(PCBuild build) {
        Long buildId = repository.savePCBuild(build);
        if (buildId != null) {
            build.setId(buildId);
            return build;
        }
        throw new RuntimeException("Failed to create build");
    }

    public Part createComponent(Part component) {
        Long partId = repository.saveComponent(component);
        if (partId != null) {
            component.setId(partId);
            return component;
        }
        throw new RuntimeException("Failed to create component");
    }

    public Part getPart(Long id) {
        Part part = repository.getPart(id);
        if (part != null) return part;
        throw new RuntimeException("Component not found");
    }

    public PCBuild addComponent(Long buildId, Part component) {
        boolean check=repository.addComponentToBuild(buildId, component);
        if (check) {
            PCBuild build = getBuild(buildId);
            build.addComponent(component);
            return build;
        }
        throw new RuntimeException("Build not found");
    }

    public String compareBuilds(Long id1, Long id2) {
        // Recupera le due build
        PCBuild build1 = repository.getPCBuild(id1);
        PCBuild build2 = repository.getPCBuild(id2);

        if (build1 == null || build2 == null) {
            throw new RuntimeException("One or both components not found");
        }

        // Popola i componenti con i dati completi dal repository
        List<Part> fullComponents1 = build1.getComponents().stream()
                .map(c -> repository.getPart(c.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        build1.setComponents(fullComponents1);

        List<Part> fullComponents2 = build2.getComponents().stream()
                .map(c -> repository.getPart(c.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        build2.setComponents(fullComponents2);

        // Ora il confronto può accedere a tutti i dati dei componenti
        return build1.compareWithBuild(build2);
    }

    public PCBuild getBuild(Long id) {
        return repository.getPCBuild(id);
    }

    public PCBuild generateOptimizedBuildByBudget(float budget) {
        if (budget <= 0)
            throw new RuntimeException("Invalid budget: " + budget + "<0");

        List<Part> allComponents = repository.getAllComponents();
        if (allComponents == null || allComponents.isEmpty())
            throw new RuntimeException("No components available");

        // Raggruppa i componenti per tipo
        Map<Class<? extends Part>, List<Part>> grouped = allComponents.stream()
                .collect(Collectors.groupingBy(Part::getClass));

        // Calcola la build minima e il suo costo
        float minTotal = 0f;
        Map<Class<? extends Part>, Part> cheapestParts = new HashMap<>();

        for (Map.Entry<Class<? extends Part>, List<Part>> entry : grouped.entrySet()) {
            List<Part> parts = entry.getValue();
            Part cheapest = parts.stream()
                    .min(Comparator.comparingDouble(Part::getPrice))
                    .orElse(null);
            if (cheapest != null) {
                cheapestParts.put(entry.getKey(), cheapest);
                minTotal += cheapest.getPrice();
            }
        }

        // Se il budget è inferiore al minimo possibile → build impossibile
        if (budget < minTotal)
            throw new RuntimeException(
                    String.format("Invalid budget. Smallest amount: %.2f", minTotal)
            );

        // Calcola pesi proporzionali in base alla build minima
        Map<Class<? extends Part>, Float> weights = new HashMap<>();
        for (Map.Entry<Class<? extends Part>, Part> entry : cheapestParts.entrySet()) {
            weights.put(entry.getKey(), entry.getValue().getPrice() / minTotal);
        }

        PCBuild build = new PCBuild();
        float totalPrice = 0f;

        // Fase 1: seleziona i componenti base secondo i pesi
        Map<Class<? extends Part>, Part> chosenParts = new HashMap<>();

        for (Map.Entry<Class<? extends Part>, List<Part>> entry : grouped.entrySet()) {
            Class<? extends Part> type = entry.getKey();
            List<Part> candidates = entry.getValue();
            float weight = weights.getOrDefault(type, 1f / grouped.size());
            float budgetForType = budget * weight;

            List<Part> sorted = candidates.stream()
                    .filter(p -> p.getPrice() <= budgetForType)
                    .sorted(Comparator.comparingDouble(p -> -(p.getPerformance() / p.getPrice())))
                    .toList();

            Part chosen = sorted.isEmpty() ? cheapestParts.get(type) : sorted.getFirst();
            chosenParts.put(type, chosen);
            build.addComponent(chosen);
            totalPrice += chosen.getPrice();
        }

        // Fase 2: usa il budget rimanente per fare upgrade
        float leftover = budget - totalPrice;

        if (leftover > 0) {
            boolean upgraded = true;

            while (upgraded && leftover > 0.01f) { // finché ci sono soldi residui e possibilità di upgrade
                upgraded = false;

                for (Map.Entry<Class<? extends Part>, List<Part>> entry : grouped.entrySet()) {
                    Class<? extends Part> type = entry.getKey();
                    List<Part> candidates = entry.getValue();
                    Part current = chosenParts.get(type);
                    if (current == null) continue;

                    // ✅ Copia leftover in una variabile finale da usare nella lambda
                    final float availableBudget = leftover;

                    List<Part> upgrades = candidates.stream()
                            .filter(p -> p.getPrice() > current.getPrice() &&
                                    p.getPrice() - current.getPrice() <= availableBudget)
                            .sorted(Comparator.comparingDouble(p -> -(p.getPerformance() / p.getPrice())))
                            .toList();

                    if (!upgrades.isEmpty()) {
                        Part bestUpgrade = upgrades.getFirst();
                        leftover -= (bestUpgrade.getPrice() - current.getPrice());
                        totalPrice += (bestUpgrade.getPrice() - current.getPrice());
                        chosenParts.put(type, bestUpgrade);
                        upgraded = true;
                        break; // riparti per riequilibrare con i nuovi costi
                    }
                }
            }

            // Aggiorna la build finale dopo gli upgrade
            build.getComponents().clear();
            build.getComponents().addAll(chosenParts.values());
        }

        if (build.getComponents().isEmpty())
            throw new RuntimeException("Invalid Budget");

        return build;
    }

    /**
     * Confronta due componenti (Part) in base alle loro prestazioni.
     * Restituisce una stringa con il risultato e la motivazione.
     */
    public String compareComponents(Long id1, Long id2) {
        Part p1 = repository.getPart(id1);
        Part p2 = repository.getPart(id2);

        if (p1 == null || p2 == null) {
            throw new RuntimeException("One or both components not found");
        }

        // Verifica che i tipi siano uguali (CPU vs CPU, GPU vs GPU, ecc.)
        if (!p1.getClass().equals(p2.getClass())) {
            return "⚠️ Mismatch component type (" +
                    p1.getClass().getSimpleName() + " vs " + p2.getClass().getSimpleName() + ")";
        }

        // Usa i metodi specializzati se disponibili (compareWithReason)
        try {
            Method method = p1.getClass().getMethod("compareWithReason", p1.getClass());
            Object result = method.invoke(p1, p2);
            return result.toString();
        } catch (NoSuchMethodException e) {
            // Fallback: confronta solo le performance e il prezzo
            float perf1 = p1.getPerformance();
            float perf2 = p2.getPerformance();

            StringBuilder sb = new StringBuilder();
            sb.append("Compare between ").append(p1.getModel()).append(" and ").append(p2.getModel()).append(":\n");

            if (perf1 > perf2) {
                sb.append("👉 ").append(p1.getModel()).append(" is more efficient.\n");
            } else if (perf1 < perf2) {
                sb.append("👉 ").append(p2.getModel()).append(" is more efficient.\n");
            } else {
                sb.append("⚖️ Same performance.\n");
            }

            sb.append("\nDetails:\n")
                    .append(p1.getModel()).append(": ").append(String.format("%.2f", perf1)).append("\n")
                    .append(p2.getModel()).append(": ").append(String.format("%.2f", perf2)).append("\n");

            int priceCompare = Float.compare(p1.getPrice(), p2.getPrice());
            if (priceCompare < 0) {
                sb.append("💰 ").append(p1.getModel()).append(" is cheaper.\n");
            } else if (priceCompare > 0) {
                sb.append("💰 ").append(p2.getModel()).append(" is cheaper.\n");
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error during comparison", e);
        }
    }

    public PCBuild updateBuild(Long id, PCBuild build) {
        boolean success = repository.updatePCBuild(id, build);
        if (success) {
            build.setId(id);
            return build;
        }
        throw new RuntimeException("Build not found");
    }

    public Part updateComponent(Long id, Part updatedPart) {
        boolean success = repository.updateComponent(id, updatedPart);
        if (success) {
            updatedPart.setId(id); // ← aggiungi questa riga
            return updatedPart;
        } else {
            throw new RuntimeException("Component not found");
        }
    }

    public void deleteBuild(Long id) {
        if (!repository.deletePCBuild(id)) {
            throw new RuntimeException("Build not found");
        }
    }

    public void deleteComponent(Long id) {
        if (!repository.deleteComponent(id)) {
            throw new RuntimeException("Component not found");
        }
    }

    public List<Part> getComponentsBySpecification(String column, String value) {
        return repository.findPartsBySpecification(column, value);
    }

    public List<PCBuild> getBuildsBySpecification(String column, String value) {
        return repository.findBuildsByComponentSpecification(column, value);
    }
}