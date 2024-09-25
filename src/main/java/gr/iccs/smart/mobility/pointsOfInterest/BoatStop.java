package gr.iccs.smart.mobility.pointsOfInterest;

import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Node
public final class BoatStop extends PointOfInterest implements ReachableNode {

    public BoatStop(UUID id, Point location) {
        super(id, location);
    }

    @Relationship(type = "PARKED_IN", direction = Relationship.Direction.INCOMING)
    private List<Vehicle> parkedVehicles = new ArrayList<>();

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public List<Vehicle> getParkedVehicles() {
        return parkedVehicles;
    }

    public void setParkedVehicles(List<Vehicle> parkedVehicles) {
        this.parkedVehicles = parkedVehicles;
    }

}
