package gr.iccs.smart.mobility.vehicle;

import java.io.Serializable;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import gr.iccs.smart.mobility.location.LocationDTO;
import jakarta.validation.constraints.NotNull;

/**
 * Vehicle info + current status
 */
@Node
public abstract sealed class Vehicle implements Serializable permits Boat, LandVehicle {
    private static final long serialVersionUID = 1L;

    @Id
    @NotNull(message = "id cannot be empty")
    private final String id;
    private final VehicleType type;
    private final Boolean dummy;

    private Point location;

    private Long battery;

    private VehicleStatus status;

    protected Vehicle(String id, VehicleType type, Boolean dummy) {
        this.id = id;
        this.type = type;
        this.dummy = dummy;
    }

    protected Vehicle(String id, VehicleType type, Boolean dummy, Long battery) {
        this(id, type, dummy);
        this.battery = battery;
    }

    protected Vehicle(String id, VehicleType type, Boolean dummy, Long battery, Point location) {
        this(id, type, dummy);
        this.battery = battery;
        this.location = location;
    }

    /*
     ********* GETTERS & SETTERS
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
        return !this.type.equals(VehicleType.SEA_VESSEL);
    }

    public VehicleDAO toVehicleDAO() {
        return new VehicleDAO(this.id, this.type, this.battery, this.dummy, LocationDTO.fromGeographicPoint(location));
    }
}
