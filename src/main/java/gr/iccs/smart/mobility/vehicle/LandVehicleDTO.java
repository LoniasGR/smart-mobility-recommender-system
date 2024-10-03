package gr.iccs.smart.mobility.vehicle;

import java.util.UUID;

import org.neo4j.driver.types.Point;

public interface LandVehicleDTO {

    UUID getId();

    VehicleType getType();

    Point getLocation();
}
