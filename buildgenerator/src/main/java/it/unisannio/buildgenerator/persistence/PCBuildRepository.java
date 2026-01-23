package it.unisannio.buildgenerator.persistence;

import it.unisannio.buildgenerator.model.PCBuild;
import it.unisannio.buildgenerator.model.Part;
import java.util.List;

public interface PCBuildRepository {

    Long saveComponent(Part part);
    Long savePCBuild(PCBuild build);

    PCBuild getPCBuild(Long id);

    Part getPart(Long id);

    boolean updatePCBuild(Long buildID, PCBuild build);

    void createDB();

    List<Part> getAllComponents();

    boolean updateComponentInBuild(Long buildId, Long oldPartid, Part part);

    boolean updateComponent(Long partID,Part part);

    boolean addComponentToBuild(Long buildID, Part part);

    List<PCBuild> findBuildsByComponentSpecification(String column, String value);

    List<Part> findPartsBySpecification(String column, String value);

    boolean deleteComponent(Long partID);

    boolean deletePCBuild(Long buildID);

    boolean closeConnection();

}