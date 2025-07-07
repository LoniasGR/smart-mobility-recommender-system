package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;

import gr.iccs.smart.mobility.connection.Connection;

@Node
public final class Car extends LandVehicle {
    public Car(String id, VehicleType type, Boolean dummy, List<Connection> connections) {
        super(id, type, dummy, connections);
    }

    public Car(String id, VehicleType type, Boolean dummy, Long battery, Point location,
            List<Connection> connections) {
        super(id, type, dummy, battery, location, connections);
    }
}