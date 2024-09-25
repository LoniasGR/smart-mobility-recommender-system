package gr.iccs.smart.mobility.connection;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public interface ReachableNode {
    public Point getLocation();
}
