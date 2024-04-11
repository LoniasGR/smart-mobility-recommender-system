package gr.iccs.smart.mobility.vehicle;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

/**
 * Vehicle info + current status
 */
@Node
public class Vehicle {
    @Id
    @NotNull(message = "id cannot be empty")
    private final UUID id;
    private final VehicleType type;

    /*
    TODO:
    We can measure distance with spatial functions of neo4j
    https://neo4j.com/docs/cypher-manual/current/functions/spatial/
    */
    private Double latitude;
    private Double longitude;
    private Float battery;

    private VehicleStatus status;

    @Version
    private Long version;

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

    public Float getBattery() {
        return battery;
    }

    public void setBattery(Float battery) {
        this.battery = battery;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
