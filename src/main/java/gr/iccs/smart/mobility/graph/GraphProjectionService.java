package gr.iccs.smart.mobility.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.internal.InternalNode;
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

    public List<Map<String, Object>> shortestPaths(String projName, String username, Integer paths) {
        String query = """
                MATCH (start:UserStartLandmark)<-[r1:IS_TRAVELLING]-(u:User{id: '$username'})
                MATCH (finish:UserDestinationLandmark)<-[r2:IS_TRAVELLING]-(u:User{id: '$username'})
                CALL gds.shortestPath.yens.stream('$name', {
                        sourceNode:start,
                        TargetNode:finish,
                        relationshipWeightProperty: 'distance',
                        k: $paths
                    })
                YIELD index, sourceNode, targetNode, totalCost, path, nodeIds,  costs
                RETURN
                    index,
                    gds.util.asNode(sourceNode) AS sourceNodeName,
                    gds.util.asNode(targetNode) AS targetNodeName,
                    [nodeId IN nodeIds | gds.util.asNode(nodeId).id] AS nodePath,
                    nodes(path) as path,
                    totalCost,
                    costs
                ORDER BY index;
                """.replace("$name", projName)
                .replace("$username", username)
                .replace("$paths", paths.toString());
        var res = client.query(query).fetch().all();

        if (res.isEmpty()) {
            return null;
        }

        // For some reason we are getting a lot more results than we
        // should, so we are filtering them here
        Long index = 0L;
        List<Map<String, Object>> ret = new ArrayList<>();
        for (Map<String, Object> map : res) {
            if (map.get("index") instanceof Long idx) {
                if (index == idx) {
                    log.info("Result {} is: {}", index, map);
                    ret.add(map);
                    index++;
                }
            }
        }
        return ret;
    }

    public void destroyGraph(String projName) {
        String query = """
                CALL gds.graph.drop('$name');
                """.replace("$name", projName);

        client.query(query).run();

    }
}
