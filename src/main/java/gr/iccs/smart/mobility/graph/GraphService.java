package gr.iccs.smart.mobility.graph;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.MovementPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.pointsOfInterest.Port;
import gr.iccs.smart.mobility.pointsOfInterest.PortService;
import gr.iccs.smart.mobility.vehicle.LandVehicle;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;

@Service
public class GraphService {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MovementPropertiesConfig config;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private PortService portService;

    private LandVehicle createConnectionWithVehicles(LandVehicle startVehicle,
            List<LandVehicle> otherVehicles) {
        for (LandVehicle v : otherVehicles) {
            if (v.getId().equals(startVehicle.getId())) {
                continue;
            }
            startVehicle = vehicleService.createConnectionTo(startVehicle, v);
        }
        return startVehicle;
    }

    private LandVehicle createConnectionWithPorts(LandVehicle vehicle, Double range) {
        List<Port> ports;
        if (range == null) {
            ports = portService.getAllWithOneLevelConnection();
        } else {
            ports = portService.getByLocationNear(vehicle.getLocation(), range);
        }
        for (var b : ports) {
            vehicle = vehicleService.createConnectionTo(vehicle, b);
        }
        return vehicle;
    }

    private void createScooterConnections(LandVehicle scooter) {
        var maxSooterDistance = config.getMaxScooterDistance();

        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                scooter.getLocation(),
                maxSooterDistance);
        scooter = createConnectionWithVehicles(scooter, surroundingVehicles);
        scooter = createConnectionWithPorts(scooter, maxSooterDistance);
    }

    private void createCarConnections(LandVehicle car) {
        // var otherVehicles =
        // vehicleService.getAllLandVehiclesWithOneLevelConnection();
        // createConnectionWithVehicles(car, otherVehicles);
        createConnectionWithPorts(car, null);
    }

    private void createPortConnections(Port port, List<Port> ports) {
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                port.getLocation(),
                config.getMaxWalkingDistance());

        for (var v : surroundingVehicles) {
            port = portService.createConnectionTo(port, v);
        }

        // TODO: Make this more complex
        for (var bb : ports) {
            if (!bb.getId().equals(port.getId()))
                port = portService.createConnectionTo(port, bb);
        }
    }

    public void graphPreCalculation() {
        var ports = portService.getAllWithOneLevelConnection();
        for (var b : ports) {
            createPortConnections(b, ports);
        }

        // First we calculate connections for all the cars
        var vehicles = vehicleService.getAllLandVehicles(LandVehicle.class);
        for (var startVehicle : vehicles) {
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
