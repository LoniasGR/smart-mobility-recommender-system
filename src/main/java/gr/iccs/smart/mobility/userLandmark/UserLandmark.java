package gr.iccs.smart.mobility.userLandmark;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import gr.iccs.smart.mobility.user.User;

@Node
public class UserLandmark {
    @Id
    @GeneratedValue
    private String id;

    private UserLandmarkType type;
    private Point location;

    @Relationship(type = "IS_TRAVELLING", direction = Relationship.Direction.INCOMING)
    private final User user;

    public UserLandmark(UserLandmarkType type, Point location, User user) {
        this.type = type;
        this.location = location;
        this.user = user;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
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

    public User getUser() {
        return this.user;
    }
}
