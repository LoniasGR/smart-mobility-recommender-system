package gr.iccs.smart.mobility.pointsofinterest;

import java.util.List;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.connection.ConnectionDTO;
import gr.iccs.smart.mobility.vehicle.Vehicle;

public interface PortWithOneLevelConnection {
    public String getId();

    public Point getLocation();

    List<ConnectionDTO> getConnections();

    List<Vehicle> getParkedVehicles();
}
