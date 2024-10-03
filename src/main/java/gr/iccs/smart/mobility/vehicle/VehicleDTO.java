package gr.iccs.smart.mobility.vehicle;

import java.util.UUID;

import gr.iccs.smart.mobility.location.LocationDTO;

public record VehicleDTO(UUID id, VehicleType type, Float battery, LocationDTO location, VehicleStatus status) {
    public static VehicleDTO fromVehicle(Vehicle vehicle) {
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getType(),
                vehicle.getBattery(),
                LocationDTO.fromGeographicPoint(vehicle.getLocation()),
                vehicle.getStatus());
    }
}
