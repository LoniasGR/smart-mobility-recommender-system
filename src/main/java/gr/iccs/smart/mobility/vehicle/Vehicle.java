package gr.iccs.smart.mobility.vehicle;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import jakarta.validation.constraints.NotNull;

/**
 * Vehicle info + current status
 */
@Node
public abstract sealed class Vehicle permits Boat, LandVehicle {
    @Id
    @NotNull(message = "id cannot be empty")
    private final String id;
    private final VehicleType type;
    private final Boolean dummy;

    /*
     * TODO:
     * We can measure distance with spatial functions of neo4j
     * https://neo4j.com/docs/cypher-manual/current/functions/spatial/
     */
    private Point location;

    private Long battery;

    private VehicleStatus status;

    public Vehicle(String id, VehicleType type, Boolean dummy) {
        this.id = id;
        this.type = type;
        this.dummy = dummy;
    }

    public Vehicle(String id, VehicleType type, Boolean dummy, Long battery) {
        this(id, type, dummy);
        this.battery = battery;
    }

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */
    public VehicleType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Long getBattery() {
        return battery;
    }

    public void setBattery(Long battery) {
        this.battery = battery;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Boolean getDummy() {
        return this.dummy;
    }

    public boolean isLandVehicle() {
        if (this.type.equals(VehicleType.SEA_VESSEL)) {
            return false;
        }
        return true;
    }
}
