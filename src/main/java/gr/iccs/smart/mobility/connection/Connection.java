package gr.iccs.smart.mobility.connection;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class Connection {
    @RelationshipId
    private String id;

    private Float distance;
    private Float time;
    private Float cost;

    @TargetNode
    private ReachableNode target;

    /*
     ******************************************************************
     * GETTERS & SETTERS
     ******************************************************************
     */

    public Float getTime() {
        return time;
    }

    public void setTime(Float time) {
        this.time = time;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    public ReachableNode getTarget() {
        return target;
    }

    public void setTarget(ReachableNode target) {
        this.target = target;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
