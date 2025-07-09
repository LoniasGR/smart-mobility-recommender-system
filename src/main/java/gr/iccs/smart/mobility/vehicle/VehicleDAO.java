package gr.iccs.smart.mobility.vehicle;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import gr.iccs.smart.mobility.location.LocationDTO;

public record VehicleDAO(@Id String id, VehicleType type, Long battery, Boolean dummy, LocationDTO location)
        implements Serializable {

    public Vehicle toVehicle() {
        if (this.type() == VehicleType.SEA_VESSEL) {
            return new Boat(this.id(), this.type(), this.dummy(), this.battery(),
                    this.location().toPoint(), null);
        } else if (this.type() == VehicleType.CAR) {
            return new Car(this.id(), this.type(), this.dummy(), this.battery(),
                    this.location().toPoint(), null);
        } else if (this.type() == VehicleType.SCOOTER) {
            return new Scooter(this.id(), this.type(), this.dummy(), this.battery(),
                    this.location().toPoint(), null);
        } else {
            throw new IllegalArgumentException("Unknown vehicle type: " + this.type());
        }
    }
}
