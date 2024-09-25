package gr.iccs.smart.mobility.vehicle;

import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node
public final class Boat extends Vehicle {
    private Integer capacity;

    public Boat(UUID id, VehicleType type, Integer capacity) {
        super(id, type);
        this.capacity = capacity;
    }

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
