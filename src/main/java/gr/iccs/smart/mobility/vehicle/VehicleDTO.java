package gr.iccs.smart.mobility.vehicle;

import java.util.UUID;

import org.neo4j.driver.types.Point;
import org.springframework.data.annotation.Id;

public record VehicleDTO(@Id UUID id, VehicleType type, Double battery, Point location, VehicleStatus status) {
    public static VehicleDTO fromVehicle(Vehicle vehicle) {
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getType(),
                vehicle.getBattery(),
                vehicle.getLocation(),
                vehicle.getStatus());
    }
}
