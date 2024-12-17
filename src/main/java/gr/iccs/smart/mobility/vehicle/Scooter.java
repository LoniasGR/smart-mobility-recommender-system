package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import org.springframework.data.neo4j.core.schema.Node;

import gr.iccs.smart.mobility.connection.Connection;

@Node
public final class Scooter extends LandVehicle {
    public Scooter(String id, VehicleType type, Boolean dummy, List<Connection> connections) {
        super(id, type, dummy, connections);
    }
}
