package gr.iccs.smart.mobility.graph;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.util.DBClientProvider;

@Service
public class GraphProjectionService {
    private final Neo4jClient client;

    private static final Logger log = LoggerFactory.getLogger(GraphProjectionService.class);

    public GraphProjectionService(DBClientProvider dbClientProvider) {
        this.client = dbClientProvider.getClient();
    }

    public void generateGraph(String projName, String nodes) {
        String query = """
                CALL gds.graph.project(
                        '$name',
                        $nodes,
                        ['CONNECTS_TO'],
                        {
                            relationshipProperties: ['distance', 'time']
                        });
                """.replace("$name", projName)
                .replace("$nodes", nodes);
        client.query(query).run();
    }

    public Map<String, Object> shortestPaths(String projName, String username) {
        String query = """
                MATCH (start:UserStartLandmark)<-[r1:IS_TRAVELLING]-(u:User{id: '$username'})
                MATCH (finish:UserDestinationLandmark)<-[r2:IS_TRAVELLING]-(u:User{id: '$username'})
                CALL gds.shortestPath.dijkstra.stream('$name', {
                        sourceNode:start,
                        TargetNode:finish,
                        relationshipWeightProperty: 'distance'
                    })
                YIELD sourceNode, targetNode, path, nodeIds, totalCost, costs
                RETURN
                    [nodeId IN nodeIds | gds.util.asNode(nodeId)] AS nodePath,
                    nodes(path) as path,
                    totalCost as totalCost,
                    costs as costs;
                """.replace("$name", projName)
                .replace("$username", username);
        var res = client.query(query).fetch().first();
        if (res.isPresent()) {
            log.info("Result is: {}", res);
            return res.get();
        }
        return null;
    }

    public void destroyGraph(String projName) {
        String query = """
                CALL gds.graph.drop('$name');
                """.replace("$name", projName);

        client.query(query).run();

    }
}
