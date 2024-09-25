package gr.iccs.smart.mobility.userLandmark;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.StartingNode;

import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserStartLandmark extends UserLandmark implements StartingNode {
    @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
    private final List<Connection> connectsTo;

    public UserStartLandmark(UUID id) {
        super(id, UserLandmarkType.START);
        this.connectsTo = new ArrayList<>();
    }

    public List<Connection> getConnectsTo() {
        return connectsTo;
    }

    public void addConnection(Connection connection) {
        this.connectsTo.add(connection);
    }

    public String getOrsProfile() {
        return "foot-walking";
    }

}
