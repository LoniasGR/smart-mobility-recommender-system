package gr.iccs.smart.mobility.usage;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class Used {

    @RelationshipId
    private String id;
    private LocalDateTime startingTime;
    private Double startingLatitude;
    private Double startingLongitude;

    private LocalDateTime endingTime;
    private Double endingLatitude;
    private Double endingLongitude;

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

    public Double getStartingLatitude() {
        return startingLatitude;
    }

    public void setStartingLatitude(Double startingLatitude) {
        this.startingLatitude = startingLatitude;
    }

    public Double getStartingLongitude() {
        return startingLongitude;
    }

    public void setStartingLongitude(Double startingLongitude) {
        this.startingLongitude = startingLongitude;
    }

    public LocalDateTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalDateTime endingTime) {
        this.endingTime = endingTime;
    }

    public Double getEndingLatitude() {
        return endingLatitude;
    }

    public void setEndingLatitude(Double endingLatitude) {
        this.endingLatitude = endingLatitude;
    }

    public Double getEndingLongitude() {
        return endingLongitude;
    }

    public void setEndingLongitude(Double endingLongitude) {
        this.endingLongitude = endingLongitude;
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

}

