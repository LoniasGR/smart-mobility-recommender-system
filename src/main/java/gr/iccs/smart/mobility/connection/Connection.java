package gr.iccs.smart.mobility.connection;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class Connection {
    @RelationshipId
    private String id;

    private final Double distance;
    private final Double time;
    private final Double cost;

    @TargetNode
    private final ReachableNode target;

    public Connection(ReachableNode target, Double distance, Double time, Double cost) {
        this.cost = cost;
        this.distance = distance;
        this.time = time;
        this.target = target;
    }

    /*
     ******************************************************************
     * GETTERS & SETTERS
     ******************************************************************
     */

    public Double getTime() {
        return time;
    }

    public Double getCost() {
        return cost;
    }

    public ReachableNode getTarget() {
        return target;
    }

    public Double getDistance() {
        return distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
