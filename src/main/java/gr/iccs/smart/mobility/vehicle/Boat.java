package gr.iccs.smart.mobility.vehicle;

import org.springframework.data.neo4j.core.schema.Node;

@Node(primaryLabel = "Sea Vessel")
public final class Boat extends Vehicle {
    private Integer capacity;

    public Boat(String id, VehicleType type, Boolean dummy, Integer capacity) {
        super(id, type, dummy);
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
