package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.connection.StartingNode;
import gr.iccs.smart.mobility.vehicle.Vehicle;

@Node
public final class Port extends PointOfInterest implements ReachableNode, StartingNode {

    public Port(UUID id, Point location, List<Connection> connections) {
        super(id, location);
        this.connections = Objects.requireNonNullElseGet(connections, ArrayList::new);
    }

    @Relationship(type = "PARKED_IN", direction = Relationship.Direction.INCOMING)
    private List<Vehicle> parkedVehicles = new ArrayList<>();

    @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
    private final List<Connection> connections;
    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public List<Vehicle> getParkedVehicles() {
        return parkedVehicles;
    }

    public void setParkedVehicles(List<Vehicle> parkedVehicles) {
        this.parkedVehicles = parkedVehicles;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    public void addConnection(Connection conn) {
        this.connections.add(conn);
    }

    @Override
    public String getOrsProfile() {
        return "foot-walking";
    }

}
