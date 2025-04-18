package gr.iccs.smart.mobility.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.openrouteservice.Directions;

@Service
public class ConnectionService {
    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

    private final Directions directions;
    private final ConnectionRepository connectionRepository;

    public ConnectionService(Directions directions, ConnectionRepository connectionRepository) {
        this.directions = directions;
        this.connectionRepository = connectionRepository;
    }

    public Connection createConnection(ReachableNode destinationNode,
            Double distance, Double time) {
        var connection = new Connection(destinationNode, distance, time, null);
        return connection;
    }

    public Connection generateConnection(StartingNode startingNode, ReachableNode destinationNode) {
        var summary = directions.getDirectionsSummary(startingNode.getOrsProfile(), startingNode.getLocation(),
                destinationNode.getLocation());
        log.debug("Received summary: {} for starting node: {} to destination node: {}", summary, startingNode,
                destinationNode);
        return createConnection(destinationNode, summary.getDistance(), summary.getDuration());
    }

    public void deleteAllConnections() {
        connectionRepository.deleteAllConnections();
    }
}
