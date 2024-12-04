package gr.iccs.smart.mobility.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.connection.StartingNode;

@Node
public sealed class LandVehicle extends Vehicle implements ReachableNode, StartingNode
        permits Car, Scooter {

    public LandVehicle(UUID id, VehicleType type, List<Connection> connections) {
        super(id, type);
        this.connections = Objects.requireNonNullElseGet(connections, ArrayList::new);
    }

    @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
    private final List<Connection> connections;

    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    public String getOrsProfile() {
        if (this.getType().equals(VehicleType.CAR)) {
            return "driving-car";
        }
        return "cycling-electric";
    }
}
