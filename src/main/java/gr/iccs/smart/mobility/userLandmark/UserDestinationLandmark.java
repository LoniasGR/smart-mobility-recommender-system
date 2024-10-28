package gr.iccs.smart.mobility.userLandmark;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;

import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.user.User;

@Node
public class UserDestinationLandmark extends UserLandmark implements ReachableNode {
    public UserDestinationLandmark(Point location, User user) {
        super(UserLandmarkType.DESTINATION, location, user);
    }
}
