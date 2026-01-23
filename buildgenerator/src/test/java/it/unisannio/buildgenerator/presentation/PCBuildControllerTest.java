package it.unisannio.buildgenerator.presentation;

import it.unisannio.buildgenerator.application.PCBuildService;
import it.unisannio.buildgenerator.model.CPU;
import it.unisannio.buildgenerator.model.PCBuild;
import it.unisannio.buildgenerator.model.Part;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PCBuildControllerTest {

    private PCBuildService service;
    private PCBuildController controller;

    @BeforeEach
    void setUp() {
        service = mock(PCBuildService.class);
        controller = new PCBuildController(service);
    }

    @Test
    void testCreateBuildSuccess() {
        PCBuild build = new PCBuild();
        when(service.createBuild(build)).thenReturn(build);

        Response response = controller.createBuild(build);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(build, response.getEntity());
    }

    @Test
    void testCreateComponentSuccess() {
        CPU cpu = new CPU();
        when(service.createComponent(cpu)).thenReturn(cpu);

        Response response = controller.createComponent(cpu);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(cpu, response.getEntity());
    }

    @Test
    void testAddComponentSuccess() {
        PCBuild build = new PCBuild();
        CPU cpu = new CPU();
        when(service.addComponent(1L, cpu)).thenReturn(build);

        Response response = controller.addComponent(1L, cpu);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(build, response.getEntity());
    }

    @Test
    void testAddComponentFailure() {
        CPU cpu = new CPU();
        when(service.addComponent(1L, cpu)).thenThrow(new RuntimeException("Build not found"));

        Response response = controller.addComponent(1L, cpu);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Build not found", response.getEntity());
    }

    @Test
    void testGetBuildSuccess() {
        PCBuild build = new PCBuild();
        when(service.getBuild(1L)).thenReturn(build);

        Response response = controller.getBuild(1L);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(build, response.getEntity());
    }

    @Test
    void testGetBuildFailure() {
        when(service.getBuild(1L)).thenThrow(new RuntimeException("Build not found"));

        Response response = controller.getBuild(1L);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Build not found", response.getEntity());
    }

    @Test
    void testGenerateBuildSuccess() {
        PCBuild build = new PCBuild();
        when(service.generateOptimizedBuildByBudget(1000f)).thenReturn(build);

        Response response = controller.generateBuild(1000f);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(build, response.getEntity());
    }

    @Test
    void testGenerateBuildFailure() {
        when(service.generateOptimizedBuildByBudget(-10f)).thenThrow(new RuntimeException("Invalid budget"));

        Response response = controller.generateBuild(-10f);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid budget"));
    }

    @Test
    void testCompareBuildsSuccess() {
        when(service.compareBuilds(1L, 2L)).thenReturn("Valid comparison");

        Response response = controller.compareBuilds(1L, 2L);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Valid comparison", response.getEntity());
    }

    @Test
    void testCompareBuildsMissingId() {
        Response response = controller.compareBuilds(null, 2L);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Both IDs must be specified", response.getEntity());
    }

    @Test
    void testCompareComponentsSuccess() {
        when(service.compareComponents(1L, 2L)).thenReturn("Compatible components");

        Response response = controller.compareComponents(1L, 2L);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Compatible components", response.getEntity());
    }

    @Test
    void testUpdateBuildSuccess() {
        PCBuild build = new PCBuild();
        when(service.updateBuild(1L, build)).thenReturn(build);

        Response response = controller.updateBuild(1L, build);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(build, response.getEntity());
    }

    @Test
    void testUpdateComponentSuccess() {
        CPU cpu = new CPU();
        when(service.updateComponent(1L, cpu)).thenReturn(cpu);

        Response response = controller.updateComponent(1L, cpu);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(cpu, response.getEntity());
    }

    @Test
    void testDeleteBuildSuccess() {
        doNothing().when(service).deleteBuild(1L);

        Response response = controller.deleteBuild(1L);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    void testDeleteComponentSuccess() {
        doNothing().when(service).deleteComponent(1L);

        Response response = controller.deleteComponent(1L);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetComponentsBySpecification() {
        CPU cpu = new CPU();
        List<Part> parts = List.of(cpu);
        when(service.getComponentsBySpecification("brand", "Intel")).thenReturn(parts);

        Response response = controller.getComponentsBySpecification("brand", "Intel");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(parts, response.getEntity());
    }

    @Test
    void testGetBuildsBySpecification() {
        PCBuild build = new PCBuild();
        List<PCBuild> builds = List.of(build);
        when(service.getBuildsBySpecification("brand", "Intel")).thenReturn(builds);

        Response response = controller.getBuildsBySpecification("brand", "Intel");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(builds, response.getEntity());
    }
}