package gr.iccs.smart.mobility.graph;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
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
    private TransportationPropertiesConfig config;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private PortService portService;

    private LandVehicle createConnectionWithVehicles(LandVehicle startVehicle,
            List<LandVehicle> otherVehicles, Double maxDistance) {
        for (LandVehicle v : otherVehicles) {
            if (v.getId().equals(startVehicle.getId())) {
                continue;
            }
            startVehicle = vehicleService.createConnectionTo(startVehicle, v, maxDistance);
        }
        return startVehicle;
    }

    private LandVehicle createConnectionWithPorts(LandVehicle vehicle, Double range) {
        List<Port> ports;
        var rangeInMeters = range;
        if (range == null) {
            ports = portService.getAllWithOneLevelConnection();
        } else {
            ports = portService.getByLocationNear(vehicle.getLocation(), range);
            // Convert range to meters for connection creation
            rangeInMeters = range * 1000;
        }
        for (var b : ports) {
            vehicle = vehicleService.createConnectionTo(vehicle, b, rangeInMeters);
        }
        return vehicle;
    }

    private void createScooterConnections(LandVehicle scooter) {
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                scooter.getLocation(),
                config.getDistances().getMaxScooterDistanceKms());
        scooter = createConnectionWithVehicles(scooter,
                surroundingVehicles,
                config.getDistances().getMaxScooterDistanceMeters());
        scooter = createConnectionWithPorts(scooter, config.getDistances().getMaxScooterDistanceKms());
        vehicleService.saveAndGet(scooter);
    }

    private void createCarConnections(LandVehicle car) {
        // var otherVehicles =
        // vehicleService.getAllLandVehiclesWithOneLevelConnection();
        // createConnectionWithVehicles(car, otherVehicles);
        createConnectionWithPorts(car, null);
        vehicleService.saveAndGet(car);

    }

    private void createPortConnections(Port port, List<Port> ports) {
        // Find all land vehicles around the port
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                port.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());

        // Connect the port with all the vehicles around it (if close enough)
        for (var v : surroundingVehicles) {
            port = portService.createConnectionFrom(port, v, config.getDistances().getMaxWalkingDistanceMeters());
        }

        // Create connections with other ports if there are boats parked
        if (port.getParkedVehicles().size() > 0) {
            for (var otherPort : ports) {
                if (!otherPort.getId().equals(port.getId())) {
                    port = portService.createConnectionFrom(port, otherPort, null);
                }
            }
        }
        portService.saveAndGet(port);
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
