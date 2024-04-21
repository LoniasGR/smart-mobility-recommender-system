package gr.iccs.smart.mobility.usage;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class Used {

    @RelationshipId
    private String id;

    private UseStatus status;
    private LocalDateTime startingTime;

    private Point startingLocation;

    private LocalDateTime endingTime;
    private Point endingLocation;

    @TargetNode
    private Vehicle vehicle;

    /*
     ******************************************************************
     * GETTERS & SETTERS
     ******************************************************************
     */

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalDateTime endingTime) {
        this.endingTime = endingTime;
    }


    public String getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public UseStatus getStatus() {
        return status;
    }

    public void setStatus(UseStatus status) {
        this.status = status;
    }

    public Point getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(Point startingLocation) {
        this.startingLocation = startingLocation;
    }

    public Point getEndingLocation() {
        return endingLocation;
    }

    public void setEndingLocation(Point endingLocation) {
        this.endingLocation = endingLocation;
    }

}

