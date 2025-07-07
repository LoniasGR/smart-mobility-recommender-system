package gr.iccs.smart.mobility.pointsofinterest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.vehicle.Vehicle;

@Node
public final class Port extends PointOfInterest {

    public Port(String id, String name, Point location, List<Vehicle> parkedVehicles, List<Connection> connections) {
        super(id, name, location, connections);
        this.parkedVehicles = Objects.requireNonNullElseGet(parkedVehicles, ArrayList::new);
    }

    @Relationship(type = "PARKED_IN", direction = Relationship.Direction.INCOMING)
    private final List<Vehicle> parkedVehicles;

    /*
     * GETTERS & SETTERS
     */

    public List<Vehicle> getParkedVehicles() {
        return parkedVehicles;
    }

    @Override
    public String getOrsProfile() {
        return "foot-walking";
    }

}
