package it.unisannio.buildgenerator.persistence;

import it.unisannio.buildgenerator.model.CPU;
import it.unisannio.buildgenerator.model.PCBuild;
import it.unisannio.buildgenerator.model.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PCBuildRepositorySQLTest {

    private Connection connection;
    private PCBuildRepositorySQL repository;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        repository = new PCBuildRepositorySQL(connection);
    }

    @Test
    void testSavePCBuildSuccess() throws Exception {
        PCBuild build = new PCBuild();
        CPU cpu = new CPU();
        cpu.setId(1L);
        build.addComponent(cpu);

        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        PreparedStatement link = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(42L);
        when(connection.prepareStatement(contains("INSERT INTO PcBuildParts"))).thenReturn(link);

        Long result = repository.savePCBuild(build);

        assertEquals(42L, result);
        verify(link).setLong(1, 42L);
        verify(link).setLong(2, 1L);
        verify(link).executeUpdate();
    }

    @Test
    void testSavePCBuildFailure() throws Exception {
        PCBuild build = new PCBuild();

        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // simulate no key returned

        Long result = repository.savePCBuild(build);

        assertNull(result);
    }

    @Test
    void testAddComponentToBuildSuccess() throws Exception {
        // Arrange
        Connection connection = mock(Connection.class);
        PCBuildRepositorySQL repository = new PCBuildRepositorySQL(connection);

        Part part = mock(Part.class);
        when(part.getId()).thenReturn(null); // Simula parte senza ID

        // Simula il salvataggio del componente
        PCBuildRepositorySQL spyRepository = spy(repository);
        doReturn(42L).when(spyRepository).saveComponent(part);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        doNothing().when(ps).setLong(anyInt(), anyLong());
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        boolean result = spyRepository.addComponentToBuild(10L, part);

        // Assert
        assertTrue(result);
        verify(ps).setLong(1, 10L);
        verify(ps).setLong(2, 42L);
        verify(ps).executeUpdate();
    }

    @Test
    void testAddComponentToBuildFailure() throws Exception {
        // Arrange
        Connection connection = mock(Connection.class);
        PCBuildRepositorySQL repository = new PCBuildRepositorySQL(connection);

        Part part = mock(Part.class);
        when(part.getId()).thenReturn(null);

        PCBuildRepositorySQL spyRepository = spy(repository);
        doReturn(42L).when(spyRepository).saveComponent(part);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        doNothing().when(ps).setLong(anyInt(), anyLong());
        when(ps.executeUpdate()).thenThrow(new SQLException("Errore DB"));

        // Act
        boolean result = spyRepository.addComponentToBuild(10L, part);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDeletePCBuildFailure() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        boolean result = repository.deletePCBuild(99L);

        assertFalse(result);
    }

    @Test
    void testDeletePCBuildSuccess() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1); // one row affected

        boolean result = repository.deletePCBuild(99L);

        assertTrue(result);
    }
}