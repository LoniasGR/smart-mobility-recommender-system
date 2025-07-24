package gr.iccs.smart.mobility.pointsofinterest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.neo4j.driver.types.Point;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.connection.StartingNode;

public abstract class PointOfInterest implements ReachableNode, StartingNode {
    @Id
    private final String id;
    private final String name;
    private Point location;

    protected PointOfInterest(String id, String name, Point location, List<Connection> connections) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.connections = Objects.requireNonNullElseGet(connections, ArrayList::new);
    }

    @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
    private final List<Connection> connections;

    /*
     * GETTERS & SETTERS
     */

    public String getId() {
        return id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    public void addConnection(Connection conn) {
        this.connections.add(conn);
    }

    public String getName() {
        return this.name;
    }
}
