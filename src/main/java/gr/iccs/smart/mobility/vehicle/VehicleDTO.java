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

    public static Vehicle toVehicle(VehicleDTO vehicleDTO) {
        if (vehicleDTO.type() == VehicleType.SEA_VESSEL) {
            return new Boat(vehicleDTO.id(), vehicleDTO.type(), vehicleDTO.dummy(), vehicleDTO.battery(),
                    vehicleDTO.location(), null);
        } else {
            return new LandVehicle(vehicleDTO.id(), vehicleDTO.type(), vehicleDTO.dummy(), vehicleDTO.battery(),
                    vehicleDTO.location(), null);
        }
    }
}
