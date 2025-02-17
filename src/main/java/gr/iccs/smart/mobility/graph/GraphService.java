package gr.iccs.smart.mobility.graph;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.pointsOfInterest.Port;
import gr.iccs.smart.mobility.pointsOfInterest.BusStop;
import gr.iccs.smart.mobility.pointsOfInterest.PointOfInterestService;
import gr.iccs.smart.mobility.vehicle.LandVehicle;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;

@Service
public class GraphService {
    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private TransportationPropertiesConfig config;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private PointOfInterestService pointOfInterestService;

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
            ports = pointOfInterestService.getAllPortsWithOneLevelConnection();
        } else {
            ports = pointOfInterestService.getPortsByLocationNear(vehicle.getLocation(), range);
            // Convert range to meters for connection creation
            rangeInMeters = range * 1000;
        }
        for (var b : ports) {
            vehicle = vehicleService.createConnectionTo(vehicle, b, rangeInMeters);
        }
        return vehicle;
    }

    private LandVehicle createConnectionWithBusStops(LandVehicle vehicle, Double range) {
        List<BusStop> busStops;
        var rangeInMeters = range;
        if (range == null) {
            busStops = pointOfInterestService.getAllBusStopsWithOneLevelConnection();
        } else {
            busStops = pointOfInterestService.getBusStopsByLocationNear(vehicle.getLocation(), range);
            // Convert range to meters for connection creation
            rangeInMeters = range * 1000;
        }
        for (var b : busStops) {
            vehicle = vehicleService.createConnectionTo(vehicle, b, rangeInMeters);
        }
        return vehicle;
    }

    private void createScooterConnections(LandVehicle scooter) {
        var startTime = Instant.now();
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                scooter.getLocation(),
                config.getDistances().getMaxScooterDistanceKms());
        var duration = Duration.between(startTime, Instant.now());
        log.debug("Finding surrounding vehicles: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        scooter = createConnectionWithVehicles(scooter,
                surroundingVehicles,
                config.getDistances().getMaxScooterDistanceMeters());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with surrounding vehicles: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        scooter = createConnectionWithPorts(scooter, config.getDistances().getMaxScooterDistanceKms());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with ports: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        scooter = createConnectionWithBusStops(scooter, config.getDistances().getMaxScooterDistanceKms());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with bus stops: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        vehicleService.saveAndGet(scooter);
        duration = Duration.between(startTime, Instant.now());
        log.debug("Saving scooter: " + duration.toMillis() + " ms");

    }

    private void createCarConnections(LandVehicle car) {
        if (config.getDistances().getMaxCarDistanceKms() != null) {
            var otherVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(car.getLocation(),
                    config.getDistances().getMaxCarDistanceKms());
            createConnectionWithVehicles(car, otherVehicles, config.getDistances().getMaxCarDistanceMeters());
        }
        createConnectionWithPorts(car, config.getDistances().getMaxCarDistanceKms());
        createConnectionWithBusStops(car, config.getDistances().getMaxCarDistanceKms());
        vehicleService.saveAndGet(car);

    }

    private void createPortConnections(Port port, List<Port> ports) {
        // Find all land vehicles around the port
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                port.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());

        // Connect the port with all the vehicles around it (if close enough)
        for (var v : surroundingVehicles) {
            port = pointOfInterestService.createConnectionFrom(port, v,
                    config.getDistances().getMaxWalkingDistanceMeters());
        }

        // Find all surrounding bus stops and check if they connect with the port
        var surroundingBusStops = pointOfInterestService.getBusStopsByLocationNear(port.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());
        for (var b : surroundingBusStops) {
            port = pointOfInterestService.createConnectionFrom(port, b,
                    config.getDistances().getMaxWalkingDistanceMeters());
        }

        // Create connections with other ports if there are boats parked
        if (port.getParkedVehicles().size() > 0) {
            for (var otherPort : ports) {
                if (!otherPort.getId().equals(port.getId())) {
                    port = pointOfInterestService.createConnectionFrom(port, otherPort, null);
                }
            }
        }
        pointOfInterestService.saveAndGet(port);
    }

    private void createBusStopConnections(BusStop busStop, List<BusStop> otherStops) {
        // Find all land vehicles around the bus stop
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                busStop.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());

        // Connect the port with all the vehicles around it (if close enough)
        for (var v : surroundingVehicles) {
            busStop = pointOfInterestService.createConnectionFrom(busStop, v,
                    config.getDistances().getMaxWalkingDistanceMeters());
        }

        // Find all surrounding ports and check if they connect with the bus stop
        var surroundingPorts = pointOfInterestService.getPortsByLocationNear(busStop.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());
        for (var b : surroundingPorts) {
            busStop = pointOfInterestService.createConnectionFrom(busStop, b,
                    config.getDistances().getMaxWalkingDistanceMeters());
        }

        // Create connections with other ports if there are boats parked
        for (var otherPort : otherStops) {
            if (!otherPort.getId().equals(busStop.getId())) {
                busStop = pointOfInterestService.createConnectionFrom(busStop, otherPort, null);
            }
        }
        pointOfInterestService.saveAndGet(busStop);
    }

    public void graphPreCalculation() {
        var startTime = Instant.now();
        var ports = pointOfInterestService.getAllPortsWithOneLevelConnection();
        var duration = Duration.between(startTime, Instant.now());
        log.debug("Getting ports: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        for (var b : ports) {
            createPortConnections(b, ports);
        }
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating ports connections took: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        var busStops = pointOfInterestService.getAllBusStopsWithOneLevelConnection();
        duration = Duration.between(startTime, Instant.now());
        log.debug("Getting bus stops: " + duration.toMillis() + " ms");

        startTime = Instant.now();
        for (var b : busStops) {
            createBusStopConnections(b, busStops);
        }
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating bus stop connections took: " + duration.toMillis() + " ms");

        // First we calculate connections for all the cars
        startTime = Instant.now();
        var vehicles = vehicleService.getAllLandVehicles(LandVehicle.class);
        duration = Duration.between(startTime, Instant.now());
        log.debug("Getting all vehicles took: " + duration.toMillis() + " ms");

        for (var startVehicle : vehicles) {
            if (startVehicle.getType().equals(VehicleType.SCOOTER)) {
                startTime = Instant.now();
                createScooterConnections(startVehicle);
                duration = Duration.between(startTime, Instant.now());
                log.debug("Creating scooter connection: " + duration.toMillis() + " ms");

            } else {
                startTime = Instant.now();
                createCarConnections(startVehicle);
                duration = Duration.between(startTime, Instant.now());
                log.debug("Creating scooter connection: " + duration.toMillis() + " ms");
            }
        }
    }

    public void graphDestruction() {
        connectionService.deleteAllConnections();
    }

}
