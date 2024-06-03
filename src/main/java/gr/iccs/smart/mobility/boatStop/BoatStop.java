package gr.iccs.smart.mobility.boatStop;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Node
public class BoatStop {
    @Id
    private final UUID id;

    private Point location;

    public BoatStop(UUID id, Point location) {
        this.id = id;
        this.location = location;
    }

    @Relationship(type = "PARKED_IN", direction = Relationship.Direction.INCOMING)
    private List<Vehicle> parkedVehicles = new ArrayList<>();

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public UUID getId() {
        return id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<Vehicle> getParkedVehicles() {
        return parkedVehicles;
    }

    public void setParkedVehicles(List<Vehicle> parkedVehicles) {
        this.parkedVehicles = parkedVehicles;
    }

}
