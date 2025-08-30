package gr.iccs.smart.mobility.connection;

import java.io.Serializable;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public interface ReachableNode extends Serializable {
    public Point getLocation();
}
