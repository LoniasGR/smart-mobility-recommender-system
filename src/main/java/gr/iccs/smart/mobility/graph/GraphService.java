package gr.iccs.smart.mobility.graph;

import java.util.List;

import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.MovementPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.vehicle.LandVehicle;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;

@Service
public class GraphService {
    private final VehicleService vehicleService;
    private final MovementPropertiesConfig config;
    private final ConnectionService connectionService;

    public GraphService(VehicleService vehicleService, MovementPropertiesConfig movementPropertiesConfig,
            ConnectionService connectionService) {
        this.config = movementPropertiesConfig;
        this.vehicleService = vehicleService;
        this.connectionService = connectionService;

    }

    private void createConnectionWithVehicles(LandVehicle startVehicle,
            List<LandVehicle> otherVehicles) {
        for (LandVehicle v : otherVehicles) {
            if (v.getId().equals(startVehicle.getId())) {
                continue;
            }
            vehicleService.createConnectionTo(startVehicle, v);
        }
    }

    private void createScooterConnections(LandVehicle scooter) {
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                scooter.getLocation(),
                config.getMaxScooterDistance());
        createConnectionWithVehicles(scooter, surroundingVehicles);
    }

    private void createCarConnections(LandVehicle car) {
        var otherVehicles = vehicleService.getAllLandVehiclesWithOneLevelConnection();
        createConnectionWithVehicles(car, otherVehicles);
    }

    public void graphPreCalculation() {
        var vehicles = vehicleService.getAllLandVehicles(LandVehicle.class);
        for (LandVehicle startVehicle : vehicles) {
            if (startVehicle.getType().equals(VehicleType.SCOOTER)) {
                createScooterConnections(startVehicle);
            } else {
                createCarConnections(startVehicle);
            }

        }
    }

    public void graphDestruction() {
        connectionService.deleteAllConnections();
    }
}
