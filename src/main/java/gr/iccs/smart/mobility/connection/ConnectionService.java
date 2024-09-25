package gr.iccs.smart.mobility.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.openrouteservice.Directions;

@Service
public class ConnectionService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

    private final Directions directions;

    public ConnectionService(Directions directions) {
        this.directions = directions;
    }

    public Connection createConnection(ReachableNode destinationNode,
            Float distance, Float time) {
        var connection = new Connection();
        connection.setTime(time);
        connection.setDistance(distance);
        connection.setTarget(destinationNode);
        return connection;
    }

    public Connection generateConnection(StartingNode startingNode, ReachableNode destinationNode) {
        var summary = directions.getDirectionsSummary(startingNode.getOrsProfile(), startingNode.getLocation(),
                destinationNode.getLocation());
        return createConnection(destinationNode, summary.getDistance(), summary.getDuration());
    }
}
