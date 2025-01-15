package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.connection.ConnectionDTO;

public interface BusStopWithOneLevelConnection {
    public String getId();

    public Point getLocation();

    List<ConnectionDTO> getConnections();
}
