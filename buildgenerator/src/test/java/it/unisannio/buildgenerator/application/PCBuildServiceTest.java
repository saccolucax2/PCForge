package it.unisannio.buildgenerator.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.unisannio.buildgenerator.model.CPU;
import it.unisannio.buildgenerator.model.Case;
import it.unisannio.buildgenerator.model.Cooler;
import it.unisannio.buildgenerator.model.GPU;
import it.unisannio.buildgenerator.model.MotherBoard;
import it.unisannio.buildgenerator.model.PCBuild;
import it.unisannio.buildgenerator.model.PSU;
import it.unisannio.buildgenerator.model.Part;
import it.unisannio.buildgenerator.model.RAM;
import it.unisannio.buildgenerator.model.SSD;
import it.unisannio.buildgenerator.persistence.PCBuildRepository;

class PCBuildServiceTest {

    private PCBuildRepository repository;
    private PCBuildService service;


    private <T extends Part> T createMockPart(Class<T> type, String model, float price, float performance) {
        T part = mock(type);
        when(part.getModel()).thenReturn(model);
        when(part.getPrice()).thenReturn(price);
        when(part.getPerformance()).thenReturn(performance);
        when(part.getId()).thenReturn(null);
        return part;
    }

    @Test
    void testCreateBuildSuccess() {
        PCBuild build = new PCBuild();
        when(repository.savePCBuild(build)).thenReturn(1L);

        PCBuild result = service.createBuild(build);

        assertEquals(1L, result.getId());
    }

    @Test
    void testCreateBuildFailure() {
        PCBuild build = new PCBuild();
        when(repository.savePCBuild(build)).thenReturn(null);
    }

    @Test
    void testCreateComponentSuccess() {
        CPU cpu = new CPU();
        when(repository.saveComponent(cpu)).thenReturn(1L);

        Part result = service.createComponent(cpu);

        assertEquals(1L, result.getId());
    }

    @Test
    void testCreateComponentFailure() {
        CPU cpu = new CPU();
        when(repository.saveComponent(cpu)).thenReturn(null);
    }

    @Test
    void testGetPartSuccess() {
        CPU cpu = new CPU();
        when(repository.getPart(1L)).thenReturn(cpu);

        Part result = service.getPart(1L);

        assertEquals(cpu, result);
    }

    @Test
    void testGetPartFailure() {
        when(repository.getPart(1L)).thenReturn(null);
    }

    @Test
    void testAddComponentSuccess() {
        PCBuild build = new PCBuild();
        CPU cpu = new CPU();
        when(repository.addComponentToBuild(1L, cpu)).thenReturn(true);
        when(repository.getPCBuild(1L)).thenReturn(build);

        PCBuild result = service.addComponent(1L, cpu);

        assertTrue(result.getComponents().contains(cpu));
    }

    @Test
    void testAddComponentFailure() {
        CPU cpu = new CPU();
        when(repository.addComponentToBuild(1L, cpu)).thenReturn(false);
    }

    @Test
    void testCompareBuildsFailure() {
        when(repository.getPCBuild(1L)).thenReturn(null);
        when(repository.getPCBuild(2L)).thenReturn(new PCBuild());
    }

    @SuppressWarnings("null")
    @Test
    void testGenerateOptimizedBuildByBudgetWithoutDatabase() {
        // Arrange: lista componenti mock
        List<Part> components = List.of(
                createMockPart(CPU.class, "Intel i5", 180f, 80f),
                createMockPart(CPU.class, "Intel i7", 250f, 100f),
                createMockPart(GPU.class, "Nvidia GTX 1650", 160f, 90f),
                createMockPart(GPU.class, "RTX 3060", 350f, 150f),
                createMockPart(RAM.class, "Corsair 16GB", 75f, 40f),
                createMockPart(RAM.class, "Corsair 32GB", 120f, 60f),
                createMockPart(SSD.class, "Samsung 970 EVO", 120f, 50f),
                createMockPart(SSD.class, "Samsung 980 PRO", 180f, 80f),
                createMockPart(MotherBoard.class, "MSI B560", 100f, 60f),
                createMockPart(MotherBoard.class, "ASUS Z690", 180f, 100f),
                createMockPart(PSU.class, "EVGA 600W", 70f, 30f),
                createMockPart(PSU.class, "Corsair 750W", 100f, 45f),
                createMockPart(Case.class, "NZXT H510", 80f, 20f),
                createMockPart(Case.class, "Lian Li O11", 150f, 40f),
                createMockPart(Cooler.class, "Cooler Master", 40f, 25f),
                createMockPart(Cooler.class, "Noctua NH-D15", 90f, 60f)
        );

        when(repository.getAllComponents()).thenReturn(components);

        // Calcolo del minimo totale possibile (somma dei prezzi minimi per categoria)
        float minTotal = components.stream()
                .collect(Collectors.groupingBy(Part::getClass,
                        Collectors.minBy(java.util.Comparator.comparingDouble(Part::getPrice))))
                .values().stream()
                .flatMap(java.util.Optional::stream)
                .map(Part::getPrice)
                .reduce(0f, Float::sum);

        // Budget leggermente superiore al minimo
        float budget = minTotal + 200f;

        // Act: generiamo la build senza salvarla
        PCBuild build = service.generateOptimizedBuildByBudget(budget);

        // Assert
        assertNotNull(build);

        // Controlla che ci sia almeno un componente per ogni categoria
        Set<Class<? extends Part>> typesInBuild = build.getComponents().stream()
                .map(Part::getClass)
                .collect(Collectors.toSet());
        assertTrue(typesInBuild.containsAll(Set.of(
                CPU.class, GPU.class, RAM.class, SSD.class,
                MotherBoard.class, PSU.class, Case.class, Cooler.class
        )));

        // Prezzo totale deve essere <= budget
        float totalPrice = (float) build.getComponents().stream()
                .mapToDouble(Part::getPrice)
                .sum();
        assertTrue(totalPrice <= budget, "Build must not exceed budget");

        // Prezzo totale deve essere >= minimo possibile
        assertTrue(totalPrice >= minTotal, "Build must not be less than minimum build");
    }

    @SuppressWarnings("null")
    @Test
    void testGenerateOptimizedBuildByBudgetInsufficientBudgetWithoutDatabase() {
        // Arrange: componenti mock
        List<Part> components = List.of(
                createMockPart(CPU.class, "Intel i5", 180f, 80f),
                createMockPart(GPU.class, "Nvidia GTX 1650", 160f, 90f),
                createMockPart(RAM.class, "Corsair 16GB", 75f, 40f)
        );
        when(repository.getAllComponents()).thenReturn(components);

        // Budget inferiore al minimo totale
        float minTotal = components.stream()
                .collect(Collectors.groupingBy(Part::getClass,
                        Collectors.minBy(java.util.Comparator.comparingDouble(Part::getPrice))))
                .values().stream()
                .flatMap(java.util.Optional::stream)
                .map(Part::getPrice)
                .reduce(0f, Float::sum);

        float insufficientBudget = minTotal - 50f;

        // Act & Assert: deve lanciare eccezione
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.generateOptimizedBuildByBudget(insufficientBudget));
        assertTrue(ex.getMessage().contains("Invalid budget"));
    }

    @Test
    void testCompareComponentsDifferentTypes() {
        CPU cpu = new CPU();
        GPU gpu = new GPU();
        cpu.setModel("CPU1");
        gpu.setModel("GPU1");

        when(repository.getPart(1L)).thenReturn(cpu);
        when(repository.getPart(2L)).thenReturn(gpu);

        String result = service.compareComponents(1L, 2L);
        assertTrue(result.contains("⚠️ Mismatch component type (" +
                cpu.getClass().getSimpleName() + " vs " + gpu.getClass().getSimpleName() + ")"));
    }

    @Test
    void testCompareComponentsMissing() {
        when(repository.getPart(1L)).thenReturn(null);
        when(repository.getPart(2L)).thenReturn(new CPU());
    }

    @Test
    void testUpdateBuildSuccess() {
        // Build esistente nel repository
        PCBuild existingBuild = new PCBuild();
        existingBuild.setId(1L);

        // Build aggiornata con nuovi componenti
        PCBuild updatedBuild = new PCBuild();
        CPU cpu = new CPU();
        updatedBuild.setComponents(List.of(cpu));

        // Simula che la build esista
        when(repository.getPCBuild(1L)).thenReturn(existingBuild);

        // Simula che l'update vada a buon fine
        when(repository.updatePCBuild(1L, updatedBuild)).thenReturn(true);

        // Esegui il test
        PCBuild result = service.updateBuild(1L, updatedBuild);

        // Verifiche
        assertEquals(1L, result.getId());
        assertEquals(1, result.getComponents().size());
        assertTrue(result.getComponents().contains(cpu));
    }

    @Test
    void testUpdateBuildFailure() {
        // Simula che la build non esista
        when(repository.getPCBuild(1L)).thenReturn(null);
   }

    @Test
    void testUpdateComponentSuccess() {
        CPU cpu = new CPU();
        when(repository.updateComponent(1L, cpu)).thenReturn(true);

        Part result = service.updateComponent(1L, cpu);

        assertEquals(1L, result.getId());
    }

    @Test
    void testUpdateComponentFailure() {
        CPU cpu = new CPU();
        when(repository.updateComponent(1L, cpu)).thenReturn(false);
    }

    @Test
    void testDeleteBuildSuccess() {
        when(repository.deletePCBuild(1L)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteBuild(1L));
    }

    @Test
    void testDeleteBuildFailure() {
        when(repository.deletePCBuild(1L)).thenReturn(false);
    }

    @Test
    void testDeleteComponentSuccess() {
        when(repository.deleteComponent(1L)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteComponent(1L));
    }

    @Test
    void testDeleteComponentFailure() {
        when(repository.deleteComponent(1L)).thenReturn(false);
    }

    @Test
    void testGetComponentsBySpecification() {
        CPU cpu = new CPU();
        when(repository.findPartsBySpecification("brand", "Intel")).thenReturn(List.of(cpu));

        List<Part> result = service.getComponentsBySpecification("brand", "Intel");

        assertEquals(1, result.size());
    }

    @Test
    void testGetBuildsBySpecification() {
        PCBuild build = new PCBuild();
        when(repository.findBuildsByComponentSpecification("brand", "Intel")).thenReturn(List.of(build));

        List<PCBuild> result = service.getBuildsBySpecification("brand", "Intel");

        assertEquals(1, result.size());
    }
}