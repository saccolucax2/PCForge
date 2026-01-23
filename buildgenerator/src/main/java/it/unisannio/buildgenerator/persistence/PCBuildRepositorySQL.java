package it.unisannio.buildgenerator.persistence;

import it.unisannio.buildgenerator.model.*;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PCBuildRepositorySQL implements PCBuildRepository {
    private static final String db = "ingsw";
    private static String host = System.getenv("MYSQL_ADDRESS");
    private static String port = System.getenv("MYSQL_PORT");

    private final Connection connection;

    private static final String CREATE_PC_BUILD_TABLE = "CREATE TABLE IF NOT EXISTS PCBuild (id BIGINT PRIMARY KEY AUTO_INCREMENT);";
    private static final String CREATE_PART_TABLE = "CREATE TABLE IF NOT EXISTS Part (id BIGINT PRIMARY KEY AUTO_INCREMENT, model VARCHAR(50), brand VARCHAR(50), price FLOAT);";
    private static final String CREATE_PC_BUILD_PARTS_TABLE = "CREATE TABLE IF NOT EXISTS PcBuildParts (pcbuild_id BIGINT, part_id BIGINT, PRIMARY KEY (pcbuild_id, part_id), FOREIGN KEY (pcbuild_id) REFERENCES PCBuild(id), FOREIGN KEY (part_id) REFERENCES Part(id));";

private static final String CREATE_CPU_TABLE = "CREATE TABLE IF NOT EXISTS CPU (id BIGINT PRIMARY KEY, cores INT, threads INT, socketType VARCHAR(30), cacheSize VARCHAR(20), freqBase FLOAT, freqBoost FLOAT, FOREIGN KEY (id) REFERENCES Part(id));";
private static final String CREATE_GPU_TABLE = "CREATE TABLE IF NOT EXISTS GPU (id BIGINT PRIMARY KEY, memory VARCHAR(30), CUDACores INT, memoryBus INT, baseClock INT, boostClock INT,TDP INT,FOREIGN KEY (id) REFERENCES Part(id));";
private static final String CREATE_CASE_TABLE = "CREATE TABLE IF NOT EXISTS Cas (id BIGINT PRIMARY KEY, formFactor VARCHAR(20), Bay VARCHAR(100), Pannelli VARCHAR(200), Ventola VARCHAR(100), FOREIGN KEY (id) REFERENCES Part(id));";
private static final String CREATE_RAM_TABLE = "CREATE TABLE IF NOT EXISTS RAM (id BIGINT PRIMARY KEY, type VARCHAR(10), capacity INT, frequency INT, CASLatency VARCHAR(20), tension FLOAT, FOREIGN KEY (id) REFERENCES Part(id));";
private static final String CREATE_PSU_TABLE = "CREATE TABLE IF NOT EXISTS PSU (id BIGINT PRIMARY KEY, wattage INT, efficiencyRating VARCHAR(30), ventola VARCHAR(30), FOREIGN KEY (id) REFERENCES Part(id))";
private static final String CREATE_PSU_CONNECTOR_TABLE = "CREATE TABLE IF NOT EXISTS PSU_Connector (psu_id BIGINT, connector VARCHAR(100), FOREIGN KEY (psu_id) REFERENCES PSU(id))";

private static final String CREATE_MOTHERBOARD_TABLE = "CREATE TABLE IF NOT EXISTS MotherBoard (id BIGINT PRIMARY KEY, chipset VARCHAR(50), socket VARCHAR(30), ramSupport VARCHAR(50), slotM2 VARCHAR(30), FOREIGN KEY (id) REFERENCES Part(id))";
private static final String CREATE_MOTHERBOARD_PCIE_TABLE = "CREATE TABLE IF NOT EXISTS MotherBoard_PCIeSlot (motherboard_id BIGINT, pcie_slot VARCHAR(30), FOREIGN KEY (motherboard_id) REFERENCES MotherBoard(id))";
private static final String CREATE_COOLER_TABLE = "CREATE TABLE IF NOT EXISTS Cooler (id BIGINT PRIMARY KEY, type VARCHAR(30), socketCompatibility VARCHAR(50), ventola VARCHAR(50), heatPipes VARCHAR(50), FOREIGN KEY (id) REFERENCES Part(id))";
private static final String CREATE_SSD_TABLE = "CREATE TABLE IF NOT EXISTS SSD (id BIGINT PRIMARY KEY, formFactor VARCHAR(20), interfaceType VARCHAR(20), capacity VARCHAR(20), readSpeed INT, writeSpeed INT, FOREIGN KEY (id) REFERENCES Part(id))";

    public PCBuildRepositorySQL() {
        if (host == null) host = "localhost";
        if (port == null) port = "3306";
        String URI = "jdbc:mysql://" + host + ":" + port + "/" + db;
        try {
            this.connection = DriverManager.getConnection(URI, "root", "mysql");
            this.createDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PCBuildRepositorySQL(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createDB() {
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate(CREATE_PC_BUILD_TABLE);
            statement.executeUpdate(CREATE_PART_TABLE);
            statement.executeUpdate(CREATE_PC_BUILD_PARTS_TABLE);
            statement.executeUpdate(CREATE_CPU_TABLE);
            statement.executeUpdate(CREATE_GPU_TABLE);
            statement.executeUpdate(CREATE_CASE_TABLE);
            statement.executeUpdate(CREATE_RAM_TABLE);
            statement.executeUpdate(CREATE_PSU_TABLE);
            statement.executeUpdate(CREATE_MOTHERBOARD_TABLE);
            statement.executeUpdate(CREATE_MOTHERBOARD_PCIE_TABLE);
            statement.executeUpdate(CREATE_PSU_CONNECTOR_TABLE);
            statement.executeUpdate(CREATE_COOLER_TABLE);
            statement.executeUpdate(CREATE_SSD_TABLE);
            // Aggiungi qui eventuali altre tabelle necessarie


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long saveComponent(Part part) {
    try {
        // Inserimento base nella tabella Part
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO Part (model, brand, price) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, part.getModel());
        ps.setString(2, part.getBrand());
        ps.setFloat(3, part.getPrice());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            long partId = rs.getLong(1);

            // Gestione sottoclasse
            switch (part) {
                case CPU cpu -> {
                    PreparedStatement cpuStmt = connection.prepareStatement(
                            "INSERT INTO CPU (id, cores, threads, socketType, cacheSize, freqBase, freqBoost) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    cpuStmt.setLong(1, partId);
                    cpuStmt.setInt(2, cpu.getCores());
                    cpuStmt.setInt(3, cpu.getThreads());
                    cpuStmt.setString(4, cpu.getSocketType());
                    cpuStmt.setString(5, cpu.getCacheSize());
                    cpuStmt.setFloat(6, cpu.getFreqBase());
                    cpuStmt.setFloat(7, cpu.getFreqBoost());
                    cpuStmt.executeUpdate();
                }
                case GPU gpu -> {
                    PreparedStatement gpuStmt = connection.prepareStatement(
                            "INSERT INTO GPU (id, memory, CUDACores, memoryBus, baseClock, boostClock, TDP) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    gpuStmt.setLong(1, partId);
                    gpuStmt.setString(2, gpu.getMemory());
                    gpuStmt.setInt(3, gpu.getCUDACores());
                    gpuStmt.setInt(4, gpu.getMemoryBus());
                    gpuStmt.setInt(5, gpu.getBaseClock());
                    gpuStmt.setInt(6, gpu.getBoostClock());
                    gpuStmt.setInt(7, gpu.getTDP());
                    gpuStmt.executeUpdate();
                }
                case RAM ram -> {
                    PreparedStatement ramStmt = connection.prepareStatement(
                            "INSERT INTO RAM (id, type, capacity, frequency, CASLatency, tension) VALUES (?, ?, ?, ?, ?, ?)");
                    ramStmt.setLong(1, partId);
                    ramStmt.setString(2, ram.getType());
                    ramStmt.setInt(3, ram.getCapacity());
                    ramStmt.setInt(4, ram.getFrequency());
                    ramStmt.setString(5, ram.getCASLatency());
                    ramStmt.setFloat(6, ram.getTension());
                    ramStmt.executeUpdate();
                }
                case PSU psu -> {
                    PreparedStatement psuStmt = connection.prepareStatement(
                            "INSERT INTO PSU (id, wattage, efficiencyRating, ventola) VALUES (?, ?, ?, ?)");
                    psuStmt.setLong(1, partId);
                    psuStmt.setInt(2, psu.getWattage());
                    psuStmt.setString(3, psu.getEfficiencyRating());
                    psuStmt.setString(4, psu.getVentola());
                    psuStmt.executeUpdate();
                    // Inserimento connettori
                    if (psu.getPrincipalConnector() != null) {
                        for (String conn : psu.getPrincipalConnector()) {
                            PreparedStatement connStmt = connection.prepareStatement(
                                    "INSERT INTO PSU_Connector (psu_id, connector) VALUES (?, ?)");
                            connStmt.setLong(1, partId);
                            connStmt.setString(2, conn);
                            connStmt.executeUpdate();
                        }
                    }
                }
                case MotherBoard mb -> {
                    PreparedStatement mbStmt = connection.prepareStatement(
                            "INSERT INTO MotherBoard (id, chipset, socket, ramSupport, slotM2) VALUES (?, ?, ?, ?, ?)");
                    mbStmt.setLong(1, partId);
                    mbStmt.setString(2, mb.getChipset());
                    mbStmt.setString(3, mb.getSocket());
                    mbStmt.setString(4, mb.getRAMSupport());
                    mbStmt.setString(5, mb.getSlotM2());
                    mbStmt.executeUpdate();
                    // Inserimento PCIeSlots
                    if (mb.getPCIeSlots() != null) {
                        for (String slot : mb.getPCIeSlots()) {
                            PreparedStatement slotStmt = connection.prepareStatement(
                                    "INSERT INTO MotherBoard_PCIeSlot (motherboard_id, pcie_slot) VALUES (?, ?)");
                            slotStmt.setLong(1, partId);
                            slotStmt.setString(2, slot);
                            slotStmt.executeUpdate();
                        }
                    }
                }
                case Case pcCase -> {
                    PreparedStatement caseStmt = connection.prepareStatement(
                            "INSERT INTO Cas (id, formFactor, Bay, Pannelli, Ventola) VALUES (?, ?, ?, ?, ?)");
                    caseStmt.setLong(1, partId);
                    caseStmt.setString(2, pcCase.getFormFactor());
                    caseStmt.setString(3, pcCase.getBay());
                    caseStmt.setString(4, pcCase.getPanelType());
                    caseStmt.setString(5, pcCase.getVentola());
                    caseStmt.executeUpdate();
                }
                case Cooler cooler -> {
                    PreparedStatement coolerStmt = connection.prepareStatement(
                            "INSERT INTO Cooler (id, type, socketCompatibility, ventola, heatPipes) VALUES (?, ?, ?, ?, ?)");
                    coolerStmt.setLong(1, partId);
                    coolerStmt.setString(2, cooler.getType());
                    coolerStmt.setString(3, cooler.getSocketCompatibility());
                    coolerStmt.setString(4, cooler.getVentola());
                    coolerStmt.setString(5, cooler.getHeatPipes());
                    coolerStmt.executeUpdate();
                }
                case SSD ssd -> {
                    PreparedStatement ssdStmt = connection.prepareStatement(
                            "INSERT INTO SSD (id, formFactor, interfaceType, capacity, readSpeed, writeSpeed) VALUES (?, ?, ?, ?, ?, ?)");
                    ssdStmt.setLong(1, partId);
                    ssdStmt.setString(2, ssd.getFormFactor());
                    ssdStmt.setString(3, ssd.getInterfaceType());
                    ssdStmt.setString(4, ssd.getCapacity());
                    ssdStmt.setInt(5, ssd.getReadSpeed());
                    ssdStmt.setInt(6, ssd.getWriteSpeed());
                    ssdStmt.executeUpdate();
                }
                default -> {
                }
            }
            return partId;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
    @Override
    public Part getPart(Long id) {
        try {
            // Recupera la riga base dalla tabella Part
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM Part WHERE id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            long partId = rs.getLong("id");
            String model = rs.getString("model");
            String brand = rs.getString("brand");
            float price = rs.getFloat("price");

            // ---- CPU ----
            try (PreparedStatement cpuStmt = connection.prepareStatement("SELECT * FROM CPU WHERE id = ?")) {
                cpuStmt.setLong(1, partId);
                ResultSet cpuRs = cpuStmt.executeQuery();
                if (cpuRs.next()) {
                    CPU cpu = new CPU();
                    cpu.setId(partId);
                    cpu.setModel(model);
                    cpu.setBrand(brand);
                    cpu.setPrice(price);
                    cpu.setCores(cpuRs.getInt("cores"));
                    cpu.setThreads(cpuRs.getInt("threads"));
                    cpu.setSocketType(cpuRs.getString("socketType"));
                    cpu.setCacheSize(cpuRs.getString("cacheSize"));
                    cpu.setFreqBase(cpuRs.getFloat("freqBase"));
                    cpu.setFreqBoost(cpuRs.getFloat("freqBoost"));
                    return cpu;
                }
            }

            // ---- GPU ----
            try (PreparedStatement gpuStmt = connection.prepareStatement("SELECT * FROM GPU WHERE id = ?")) {
                gpuStmt.setLong(1, partId);
                ResultSet gpuRs = gpuStmt.executeQuery();
                if (gpuRs.next()) {
                    GPU gpu = new GPU();
                    gpu.setId(partId);
                    gpu.setModel(model);
                    gpu.setBrand(brand);
                    gpu.setPrice(price);
                    gpu.setMemory(gpuRs.getString("memory"));
                    gpu.setCUDACores(gpuRs.getInt("CUDACores"));
                    gpu.setMemoryBus(gpuRs.getInt("memoryBus"));
                    gpu.setBaseClock(gpuRs.getInt("baseClock"));
                    gpu.setBoostClock(gpuRs.getInt("boostClock"));
                    gpu.setTDP(gpuRs.getInt("TDP"));
                    return gpu;
                }
            }

            // ---- RAM ----
            try (PreparedStatement ramStmt = connection.prepareStatement("SELECT * FROM RAM WHERE id = ?")) {
                ramStmt.setLong(1, partId);
                ResultSet ramRs = ramStmt.executeQuery();
                if (ramRs.next()) {
                    RAM ram = new RAM();
                    ram.setId(partId);
                    ram.setModel(model);
                    ram.setBrand(brand);
                    ram.setPrice(price);
                    ram.setType(ramRs.getString("type"));
                    ram.setCapacity(ramRs.getInt("capacity"));
                    ram.setFrequency(ramRs.getInt("frequency"));
                    ram.setCASLatency(ramRs.getString("CASLatency"));
                    ram.setTension(ramRs.getFloat("tension"));
                    return ram;
                }
            }

            // ---- PSU ----
            try (PreparedStatement psuStmt = connection.prepareStatement("SELECT * FROM PSU WHERE id = ?")) {
                psuStmt.setLong(1, partId);
                ResultSet psuRs = psuStmt.executeQuery();
                if (psuRs.next()) {
                    PSU psu = new PSU();
                    psu.setId(partId);
                    psu.setModel(model);
                    psu.setBrand(brand);
                    psu.setPrice(price);
                    psu.setWattage(psuRs.getInt("wattage"));
                    psu.setEfficiencyRating(psuRs.getString("efficiencyRating"));
                    psu.setVentola(psuRs.getString("ventola"));

                    List<String> connectors = new ArrayList<>();
                    try (PreparedStatement connStmt = connection.prepareStatement("SELECT connector FROM PSU_Connector WHERE psu_id = ?")) {
                        connStmt.setLong(1, partId);
                        ResultSet connRs = connStmt.executeQuery();
                        while (connRs.next()) {
                            connectors.add(connRs.getString("connector"));
                        }
                    }
                    psu.setPrincipalConnector(connectors);
                    return psu;
                }
            }

            // ---- Cooler ----
            try (PreparedStatement coolerStmt = connection.prepareStatement("SELECT * FROM Cooler WHERE id = ?")) {
                coolerStmt.setLong(1, partId);
                ResultSet coolerRs = coolerStmt.executeQuery();
                if (coolerRs.next()) {
                    Cooler cooler = new Cooler();
                    cooler.setId(partId);
                    cooler.setModel(model);
                    cooler.setBrand(brand);
                    cooler.setPrice(price);
                    cooler.setType(coolerRs.getString("type"));
                    cooler.setSocketCompatibility(coolerRs.getString("socketCompatibility"));
                    cooler.setVentola(coolerRs.getString("ventola"));
                    cooler.setHeatPipes(coolerRs.getString("heatPipes"));
                    return cooler;
                }
            }

            // ---- MotherBoard ----
            try (PreparedStatement mbStmt = connection.prepareStatement("SELECT * FROM MotherBoard WHERE id = ?")) {
                mbStmt.setLong(1, partId);
                ResultSet mbRs = mbStmt.executeQuery();
                if (mbRs.next()) {
                    MotherBoard mb = new MotherBoard();
                    mb.setId(partId);
                    mb.setModel(model);
                    mb.setBrand(brand);
                    mb.setPrice(price);
                    mb.setChipset(mbRs.getString("chipset"));
                    mb.setSocket(mbRs.getString("socket"));
                    mb.setRAMSupport(mbRs.getString("ramSupport"));
                    mb.setSlotM2(mbRs.getString("slotM2"));

                    List<String> slots = new ArrayList<>();
                    try (PreparedStatement slotStmt = connection.prepareStatement("SELECT pcie_slot FROM MotherBoard_PCIeSlot WHERE motherboard_id = ?")) {
                        slotStmt.setLong(1, partId);
                        ResultSet slotRs = slotStmt.executeQuery();
                        while (slotRs.next()) {
                            slots.add(slotRs.getString("pcie_slot"));
                        }
                    }
                    mb.setPCIeSlots(slots);
                    return mb;
                }
            }

            // ---- SSD ----
            try (PreparedStatement ssdStmt = connection.prepareStatement("SELECT * FROM SSD WHERE id = ?")) {
                ssdStmt.setLong(1, partId);
                ResultSet ssdRs = ssdStmt.executeQuery();
                if (ssdRs.next()) {
                    SSD ssd = new SSD();
                    ssd.setId(partId);
                    ssd.setModel(model);
                    ssd.setBrand(brand);
                    ssd.setPrice(price);
                    ssd.setFormFactor(ssdRs.getString("formFactor"));
                    ssd.setInterfaceType(ssdRs.getString("interfaceType"));
                    ssd.setCapacity(ssdRs.getString("capacity"));
                    ssd.setReadSpeed(ssdRs.getInt("readSpeed"));
                    ssd.setWriteSpeed(ssdRs.getInt("writeSpeed"));
                    return ssd;
                }
            }

            // ---- Case ----
            try (PreparedStatement caseStmt = connection.prepareStatement("SELECT * FROM Cas WHERE id = ?")) {
                caseStmt.setLong(1, partId);
                ResultSet caseRs = caseStmt.executeQuery();
                if (caseRs.next()) {
                    Case pcCase = new Case();
                    pcCase.setId(partId);
                    pcCase.setModel(model);
                    pcCase.setBrand(brand);
                    pcCase.setPrice(price);
                    pcCase.setFormFactor(caseRs.getString("formFactor"));
                    pcCase.setBay(caseRs.getString("Bay"));
                    pcCase.setPanelType(caseRs.getString("Pannelli"));
                    pcCase.setVentola(caseRs.getString("Ventola"));
                    return pcCase;
                }
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero della parte con id=" + id, e);
        }
    }


    @Override
    public List<Part> getAllComponents() {
        List<Part> componentsList = new ArrayList<>();
        try {
            // Recupera tutti gli ID dalla tabella Part
            PreparedStatement ps = connection.prepareStatement("SELECT id FROM Part");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
                Part part = getPart(id); // Riutilizza il metodo esistente per caricare la sottoclasse corretta
                if (part != null) {
                    componentsList.add(part);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return componentsList;
    }

    @Override
    public Long savePCBuild(PCBuild build) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO PCBuild VALUES ()", Statement.RETURN_GENERATED_KEYS);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long buildId = rs.getLong(1);

                for (Part part : build.getComponents()) {
                    Long partId;

                    // 🔍 Se la parte ha già un ID valido, non la reinserire
                    if (part.getId() != null) {
                        partId = part.getId();
                    } else {
                        partId = saveComponent(part);
                    }

                    PreparedStatement link = connection.prepareStatement(
                            "INSERT INTO PcBuildParts (pcbuild_id, part_id) VALUES (?, ?)");
                    link.setLong(1, buildId);
                    link.setLong(2, partId);
                    link.executeUpdate();
                }

                return buildId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



@Override
public PCBuild getPCBuild(Long id) {
    try {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM PCBuild WHERE id = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            PCBuild build = new PCBuild();
            build.setId(id);
            PreparedStatement partsStmt = connection.prepareStatement("SELECT part_id FROM PcBuildParts WHERE pcbuild_id = ?");
            partsStmt.setLong(1, id);
            ResultSet partsRs = partsStmt.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (partsRs.next()) {
                Part part = getPart(partsRs.getLong("part_id"));
                if (part != null) parts.add(part);
            }
            build.setComponents(parts);
            return build;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

    @Override
    public boolean updatePCBuild(Long buildId, PCBuild build) {
        try {
            // 1. Verifica che la build esista
            PreparedStatement check = connection.prepareStatement(
                    "SELECT id FROM PCBuild WHERE id = ?");
            check.setLong(1, buildId);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                return false; // build non esistente
            }

            // 2. Cancella i componenti associati
            PreparedStatement deleteLinks = connection.prepareStatement(
                    "DELETE FROM PcBuildParts WHERE pcbuild_id = ?");
            deleteLinks.setLong(1, buildId);
            deleteLinks.executeUpdate();

            // 3. Inserisci i nuovi componenti
            for (Part part : build.getComponents()) {
                Long partId;

                if (part.getId() != null) {
                    partId = part.getId();
                } else {
                    partId = saveComponent(part);
                }

                PreparedStatement link = connection.prepareStatement(
                        "INSERT INTO PcBuildParts (pcbuild_id, part_id) VALUES (?, ?)");
                link.setLong(1, buildId);
                link.setLong(2, partId);
                link.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


@Override
public boolean updateComponentInBuild(Long buildId, Long oldPartId, Part newPart) {
    try {
        // Rimuovi il collegamento della vecchia componente
        PreparedStatement deleteLink = connection.prepareStatement(
            "DELETE FROM PcBuildParts WHERE pcbuild_id = ? AND part_id = ?");
        deleteLink.setLong(1, buildId);
        deleteLink.setLong(2, oldPartId);
        deleteLink.executeUpdate();

        // Salva la nuova componente
        Long newPartId = saveComponent(newPart);

        // Collega la nuova componente alla build
        PreparedStatement insertLink = connection.prepareStatement(
            "INSERT INTO PcBuildParts (pcbuild_id, part_id) VALUES (?, ?)");
        insertLink.setLong(1, buildId);
        insertLink.setLong(2, newPartId);
        insertLink.executeUpdate();

        return true;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

@Override
public boolean updateComponent(Long partId, Part updatedPart) {
    try {
        // Aggiorna la tabella Part
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE Part SET model = ?, brand = ?, price = ? WHERE id = ?");
        ps.setString(1, updatedPart.getModel());
        ps.setString(2, updatedPart.getBrand());
        ps.setFloat(3, updatedPart.getPrice());
        ps.setLong(4, partId);
        ps.executeUpdate();

        // Aggiorna la sotto tabella specifica
        switch (updatedPart) {
            case CPU cpu -> {
                PreparedStatement cpuStmt = connection.prepareStatement(
                        "UPDATE CPU SET cores = ?, threads = ?, socketType = ?, cacheSize = ?, freqBase = ?, freqBoost = ? WHERE id = ?");
                cpuStmt.setInt(1, cpu.getCores());
                cpuStmt.setInt(2, cpu.getThreads());
                cpuStmt.setString(3, cpu.getSocketType());
                cpuStmt.setString(4, cpu.getCacheSize());
                cpuStmt.setFloat(5, cpu.getFreqBase());
                cpuStmt.setFloat(6, cpu.getFreqBoost());
                cpuStmt.setLong(7, partId);
                cpuStmt.executeUpdate();
            }
            case GPU gpu -> {
                PreparedStatement gpuStmt = connection.prepareStatement(
                        "UPDATE GPU SET memory = ?, CUDACores = ?, memoryBus = ?, baseClock = ?, boostClock = ?, TDP = ? WHERE id = ?");
                gpuStmt.setString(1, gpu.getMemory());
                gpuStmt.setInt(2, gpu.getCUDACores());
                gpuStmt.setInt(3, gpu.getMemoryBus());
                gpuStmt.setInt(4, gpu.getBaseClock());
                gpuStmt.setInt(5, gpu.getBoostClock());
                gpuStmt.setInt(6, gpu.getTDP());
                gpuStmt.setLong(7, partId);
                gpuStmt.executeUpdate();
            }
            case RAM ram -> {
                PreparedStatement ramStmt = connection.prepareStatement(
                        "UPDATE RAM SET type = ?, capacity = ?, frequency = ?, CASLatency = ?, tension = ? WHERE id = ?");
                ramStmt.setString(1, ram.getType());
                ramStmt.setInt(2, ram.getCapacity());
                ramStmt.setInt(3, ram.getFrequency());
                ramStmt.setString(4, ram.getCASLatency());
                ramStmt.setFloat(5, ram.getTension());
                ramStmt.setLong(6, partId);
                ramStmt.executeUpdate();
            }
            case PSU psu -> {
                PreparedStatement psuStmt = connection.prepareStatement(
                        "UPDATE PSU SET wattage = ?, efficiencyRating = ?, ventola = ? WHERE id = ?");
                psuStmt.setInt(1, psu.getWattage());
                psuStmt.setString(2, psu.getEfficiencyRating());
                psuStmt.setString(3, psu.getVentola());
                psuStmt.setLong(4, partId);
                psuStmt.executeUpdate();
                // Aggiorna connettori: cancella e reinserisci
                PreparedStatement delConn = connection.prepareStatement("DELETE FROM PSU_Connector WHERE psu_id = ?");
                delConn.setLong(1, partId);
                delConn.executeUpdate();
                if (psu.getPrincipalConnector() != null) {
                    for (String conn : psu.getPrincipalConnector()) {
                        PreparedStatement connStmt = connection.prepareStatement(
                                "INSERT INTO PSU_Connector (psu_id, connector) VALUES (?, ?)");
                        connStmt.setLong(1, partId);
                        connStmt.setString(2, conn);
                        connStmt.executeUpdate();
                    }
                }
            }
            case MotherBoard mb -> {
                PreparedStatement mbStmt = connection.prepareStatement(
                        "UPDATE MotherBoard SET chipset = ?, socket = ?, ramSupport = ?, slotM2 = ? WHERE id = ?");
                mbStmt.setString(1, mb.getChipset());
                mbStmt.setString(2, mb.getSocket());
                mbStmt.setString(3, mb.getRAMSupport());
                mbStmt.setString(4, mb.getSlotM2());
                mbStmt.setLong(5, partId);
                mbStmt.executeUpdate();
                // Aggiorna PCIeSlots: cancella e reinserisci
                PreparedStatement delSlot = connection.prepareStatement("DELETE FROM MotherBoard_PCIeSlot WHERE motherboard_id = ?");
                delSlot.setLong(1, partId);
                delSlot.executeUpdate();
                if (mb.getPCIeSlots() != null) {
                    for (String slot : mb.getPCIeSlots()) {
                        PreparedStatement slotStmt = connection.prepareStatement(
                                "INSERT INTO MotherBoard_PCIeSlot (motherboard_id, pcie_slot) VALUES (?, ?)");
                        slotStmt.setLong(1, partId);
                        slotStmt.setString(2, slot);
                        slotStmt.executeUpdate();
                    }
                }
            }
            case Case pcCase -> {
                PreparedStatement caseStmt = connection.prepareStatement(
                        "UPDATE Cas SET formFactor = ?, Bay = ?, Pannelli=?, Ventola=? WHERE id = ?");
                caseStmt.setString(1, pcCase.getFormFactor());
                caseStmt.setString(2, pcCase.getBay());
                caseStmt.setString(3, pcCase.getPanelType());
                caseStmt.setString(4, pcCase.getVentola());
                caseStmt.setLong(3, partId);
                caseStmt.executeUpdate();
            }
            case Cooler cooler -> {
                PreparedStatement coolerStmt = connection.prepareStatement(
                        "UPDATE Cooler SET type = ?, socketCompatibility = ?, ventola = ?, heatPipes = ? WHERE id = ?");
                coolerStmt.setString(1, cooler.getType());
                coolerStmt.setString(2, cooler.getSocketCompatibility());
                coolerStmt.setString(3, cooler.getVentola());
                coolerStmt.setString(4, cooler.getHeatPipes());
                coolerStmt.setLong(5, partId);
                coolerStmt.executeUpdate();
            }
            case SSD ssd -> {
                PreparedStatement ssdStmt = connection.prepareStatement(
                        "UPDATE SSD SET formFactor = ?, interfaceType = ?, capacity = ?, readSpeed = ?, writeSpeed = ? WHERE id = ?");
                ssdStmt.setString(1, ssd.getFormFactor());
                ssdStmt.setString(2, ssd.getInterfaceType());
                ssdStmt.setString(3, ssd.getCapacity());
                ssdStmt.setInt(4, ssd.getReadSpeed());
                ssdStmt.setInt(5, ssd.getWriteSpeed());
                ssdStmt.setLong(6, partId);
                ssdStmt.executeUpdate();
            }
            default -> {
            }
        }
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

    @Override
    public boolean deleteComponent(Long partId) {
        try {
            // Elimina i collegamenti con le build
            PreparedStatement delLinks = connection.prepareStatement("DELETE FROM PcBuildParts WHERE part_id = ?");
            delLinks.setLong(1, partId);
            delLinks.executeUpdate();

            // Elimina dalle sotto tabelle specifiche
            connection.prepareStatement("DELETE FROM CPU WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM GPU WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM RAM WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM PSU_Connector WHERE psu_id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM PSU WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM MotherBoard_PCIeSlot WHERE motherboard_id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM MotherBoard WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM Cas WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM Cooler WHERE id = " + partId).executeUpdate();
            connection.prepareStatement("DELETE FROM SSD WHERE id = " + partId).executeUpdate();

            // Elimina dalla tabella Part (controllo righe eliminate)
            PreparedStatement delPart = connection.prepareStatement("DELETE FROM Part WHERE id = ?");
            delPart.setLong(1, partId);
            int affected = delPart.executeUpdate();

            return affected > 0; // true solo se almeno una riga Part eliminata
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean deletePCBuild(Long buildId) {
        try {
            // Elimina i collegamenti con le parti
            PreparedStatement delLinks = connection.prepareStatement("DELETE FROM PcBuildParts WHERE pcbuild_id = ?");
            delLinks.setLong(1, buildId);
            delLinks.executeUpdate();

            // Elimina la build dalla tabella PCBuild
            PreparedStatement delBuild = connection.prepareStatement("DELETE FROM PCBuild WHERE id = ?");
            delBuild.setLong(1, buildId);
            int affected = delBuild.executeUpdate(); // ✅ controlla righe eliminate

            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

@Override
public boolean addComponentToBuild(Long buildId, Part part) {
    try {
        Long partId = saveComponent(part);
        PreparedStatement link = connection.prepareStatement("INSERT INTO PcBuildParts (pcbuild_id, part_id) VALUES (?, ?)");
        link.setLong(1, buildId);
        link.setLong(2, partId);
        link.executeUpdate();
        return true;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

@Override
public List<PCBuild> findBuildsByComponentSpecification(String column, String value) {
    List<PCBuild> builds = new ArrayList<>();
    try {
        // Trova tutti i part_id che corrispondono alla specifica
        String partQuery = "SELECT id FROM Part WHERE " + column + " = ?";
        PreparedStatement partPs = connection.prepareStatement(partQuery);
        partPs.setString(1, value);
        ResultSet partRs = partPs.executeQuery();
        List<Long> partIds = new ArrayList<>();
        while (partRs.next()) {
            partIds.add(partRs.getLong("id"));
        }
        if (partIds.isEmpty()) return builds;

        // Trova tutte le build che contengono almeno una di queste componenti
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < partIds.size(); i++) {
            inClause.append("?");
            if (i < partIds.size() - 1) inClause.append(",");
        }
        String buildQuery = "SELECT DISTINCT pcbuild_id FROM PcBuildParts WHERE part_id IN (" + inClause + ")";
        PreparedStatement buildPs = connection.prepareStatement(buildQuery);
        for (int i = 0; i < partIds.size(); i++) {
            buildPs.setLong(i + 1, partIds.get(i));
        }
        ResultSet buildRs = buildPs.executeQuery();
        while (buildRs.next()) {
            PCBuild build = getPCBuild(buildRs.getLong("pcbuild_id"));
            if (build != null) builds.add(build);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return builds;
}

@Override
public List<Part> findPartsBySpecification(String column, String value) {
    List<Part> result = new ArrayList<>();
    try {
        String query = "SELECT id FROM Part WHERE " + column + " = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Part part = getPart(rs.getLong("id"));
            if (part != null) result.add(part);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return result;
}

    @Override
    public boolean closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}