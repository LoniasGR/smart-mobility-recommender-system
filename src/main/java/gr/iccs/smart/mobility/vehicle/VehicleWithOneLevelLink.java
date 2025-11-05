package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.connection.ConnectionDTO;

public interface VehicleWithOneLevelLink {
    String getId();

    VehicleType getType();

    VehicleStatus getStatus();

    Long getBattery();

    Point getLocation();

    List<ConnectionDTO> getConnections();
}