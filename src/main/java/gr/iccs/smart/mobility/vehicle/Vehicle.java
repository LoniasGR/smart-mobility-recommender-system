package gr.iccs.smart.mobility.vehicle;

import jakarta.validation.constraints.NotNull;
import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

/**
 * Vehicle info + current status
 */
@Node
public abstract sealed class Vehicle permits Boat, LandVehicle {
    @Id
    @NotNull(message = "id cannot be empty")
    private final UUID id;
    private final VehicleType type;

    /*
     * TODO:
     * We can measure distance with spatial functions of neo4j
     * https://neo4j.com/docs/cypher-manual/current/functions/spatial/
     */
    private Point location;
    private Double battery;

    private VehicleStatus status;

    public Vehicle(UUID id, VehicleType type) {
        this.id = id;
        this.type = type;
    }

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */
    public VehicleType getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Double getBattery() {
        return battery;
    }

    public void setBattery(Double battery) {
        this.battery = battery;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public boolean isLandVehicle() {
        if (this.type.equals(VehicleType.SEA_VESSEL)) {
            return false;
        }
        return true;
    }
}
