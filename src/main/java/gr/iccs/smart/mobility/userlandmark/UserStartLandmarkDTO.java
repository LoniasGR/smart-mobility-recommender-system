package gr.iccs.smart.mobility.userlandmark;

import java.util.List;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.connection.ConnectionDTO;

public interface UserStartLandmarkDTO {
    String getId();

    Point getLocation();

    List<ConnectionDTO> getConnections();
}
