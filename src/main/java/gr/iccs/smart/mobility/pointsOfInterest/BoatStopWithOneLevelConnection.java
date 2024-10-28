package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;
import java.util.UUID;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.connection.ConnectionDTO;

public interface BoatStopWithOneLevelConnection {
    public UUID getId();

    public Point getLocation();

    List<ConnectionDTO> getConnections();
}
