package gr.iccs.smart.mobility.connection;

import java.io.Serializable;

import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class Connection implements Serializable {
    @RelationshipId
    private Long id;

    @Version
    private Long version;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
