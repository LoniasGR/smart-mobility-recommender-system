package gr.iccs.smart.mobility.vehicle;

import org.neo4j.driver.types.Point;

public interface LandVehicleDTO {

    String getId();

    VehicleType getType();

    Point getLocation();
}
