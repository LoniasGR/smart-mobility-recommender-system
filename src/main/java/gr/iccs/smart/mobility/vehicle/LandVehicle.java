package gr.iccs.smart.mobility.vehicle;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.connection.StartingNode;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Node
public final class LandVehicle extends Vehicle implements ReachableNode, StartingNode {
    public LandVehicle(UUID id, VehicleType type, List<Connection> connections) {
        super(id, type);
        this.connections = Objects.requireNonNullElseGet(connections, ArrayList::new);
    }

    @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
    private final List<Connection> connections;

    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }

    public String getOrsProfile() {
        if (this.getType().equals(VehicleType.CAR)) {
            return "driving-car";
        }
        return "cycling-electric";
    }
}
