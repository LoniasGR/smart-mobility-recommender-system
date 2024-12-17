package gr.iccs.smart.mobility.vehicle;

import org.neo4j.driver.types.Point;
import org.springframework.data.annotation.Id;

public record VehicleDTO(@Id String id, VehicleType type, Long battery, Boolean dummy, Point location,
        VehicleStatus status) {
    public static VehicleDTO fromVehicle(Vehicle vehicle) {
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getType(),
                vehicle.getBattery(),
                vehicle.getDummy(),
                vehicle.getLocation(),
                vehicle.getStatus());
    }
}
