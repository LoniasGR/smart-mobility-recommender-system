package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.UUID;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.connection.ConnectionDTO;

public interface LandVehicleWithOneLevelLink {
    UUID getId();

    VehicleType getType();

    Point getLocation();

    List<ConnectionDTO> getConnections();
}