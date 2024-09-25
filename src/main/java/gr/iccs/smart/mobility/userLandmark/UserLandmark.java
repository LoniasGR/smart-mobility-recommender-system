package gr.iccs.smart.mobility.userLandmark;

import gr.iccs.smart.mobility.connection.ReachableNode;
import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node
public class UserLandmark implements ReachableNode {
    @Id
    private final UUID id;

    private UserLandmarkType type;
    private Point location;


    public UserLandmark(UUID id, UserLandmarkType type) {
        this.id = id;
        this.type = type;
    }

    public UUID getId() {
        return this.id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public UserLandmarkType getType() {
        return type;
    }

    public void setType(UserLandmarkType type) {
        this.type = type;
    }
}
