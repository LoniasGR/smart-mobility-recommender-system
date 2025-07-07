package gr.iccs.smart.mobility.userlandmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Relationship;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.StartingNode;
import gr.iccs.smart.mobility.user.User;

public class UserStartLandmark extends UserLandmark implements StartingNode {
    @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
    private final List<Connection> connections;

    public UserStartLandmark(Point location, List<Connection> connections, User user) {
        super(UserLandmarkType.START, location, user);
        this.connections = Objects.requireNonNullElseGet(connections, ArrayList::new);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }

    public String getOrsProfile() {
        return "foot-walking";
    }

}
