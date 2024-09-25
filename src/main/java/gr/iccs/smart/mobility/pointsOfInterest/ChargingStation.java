package gr.iccs.smart.mobility.pointsOfInterest;

import org.neo4j.driver.types.Point;

import java.util.UUID;

public class ChargingStation extends PointOfInterest {

    public ChargingStation(UUID id, Point location) {
        super(id, location);
    }
}
