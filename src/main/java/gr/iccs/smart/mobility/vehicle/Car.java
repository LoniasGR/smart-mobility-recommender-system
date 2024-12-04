package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Node;

import gr.iccs.smart.mobility.connection.Connection;

@Node
public final class Car extends LandVehicle {
    public Car(UUID id, VehicleType type, List<Connection> connections) {
        super(id, type, connections);
    }
}