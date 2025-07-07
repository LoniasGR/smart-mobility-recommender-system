package gr.iccs.smart.mobility.graph;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.pointsofinterest.BusStop;
import gr.iccs.smart.mobility.pointsofinterest.PointOfInterestService;
import gr.iccs.smart.mobility.pointsofinterest.Port;
import gr.iccs.smart.mobility.vehicle.LandVehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDBService;
import gr.iccs.smart.mobility.vehicle.VehicleType;
import gr.iccs.smart.mobility.vehicle.VehicleUtilitiesService;

@Service
public class GraphService {
    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    private final VehicleUtilitiesService vehicleUtilitiesService;
    private final VehicleDBService vehicleDBService;
    private final TransportationPropertiesConfig config;
    private final ConnectionService connectionService;
    private final PointOfInterestService pointOfInterestService;

    GraphService(VehicleUtilitiesService vehicleUtilitiesService, VehicleDBService vehicleDBService,
            TransportationPropertiesConfig config, ConnectionService connectionService,
            PointOfInterestService pointOfInterestService) {
        this.vehicleUtilitiesService = vehicleUtilitiesService;
        this.vehicleDBService = vehicleDBService;
        this.config = config;
        this.connectionService = connectionService;
        this.pointOfInterestService = pointOfInterestService;
    }

    private LandVehicle createConnectionWithVehicles(LandVehicle startVehicle, List<LandVehicle> otherVehicles,
            Double maxDistance) {
        for (LandVehicle v : otherVehicles) {
            if (v.getId().equals(startVehicle.getId())) {
                continue;
            }
            startVehicle = vehicleUtilitiesService.createConnectionTo(startVehicle, v, maxDistance);
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
            vehicle = vehicleUtilitiesService.createConnectionTo(vehicle, b, rangeInMeters);
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
            vehicle = vehicleUtilitiesService.createConnectionTo(vehicle, b, rangeInMeters);
        }
        return vehicle;
    }

    private void createScooterConnections(LandVehicle scooter) {
        var functionStartTime = Instant.now();
        var startTime = Instant.now();
        var surroundingVehicles = vehicleDBService.findLandVehicleWithOneLevelConnectionNearLocation(
                scooter.getLocation(), config.getDistances().getMaxScooterDistanceKms());
        var duration = Duration.between(startTime, Instant.now());
        log.debug("Finding surrounding vehicles: {} ms", duration.toMillis());

        startTime = Instant.now();
        scooter = createConnectionWithVehicles(scooter, surroundingVehicles,
                config.getDistances().getMaxScooterDistanceMeters());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with surrounding vehicles: {} ms", duration.toMillis());

        startTime = Instant.now();
        scooter = createConnectionWithPorts(scooter, config.getDistances().getMaxScooterDistanceKms());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with ports: {} ms", duration.toMillis());

        startTime = Instant.now();
        scooter = createConnectionWithBusStops(scooter, config.getDistances().getMaxScooterDistanceKms());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with bus stops: {} ms", duration.toMillis());

        startTime = Instant.now();
        vehicleDBService.saveAndGet(scooter);
        duration = Duration.between(startTime, Instant.now());
        log.debug("Saving scooter: {} ms", duration.toMillis());
        duration = Duration.between(functionStartTime, Instant.now());
        log.debug("Creating scooter connection: {} ms", duration.toMillis());
    }

    public void createIncomingConnections(LandVehicle vehicle) {
        var startTime = Instant.now();
        var surroundingScooters = vehicleDBService.findScooterNolConnectionNearLocation(vehicle.getLocation(),
                config.getDistances().getMaxScooterDistanceKms());
        var duration = Duration.between(startTime, Instant.now());
        log.debug("Finding surrounding vehicles: {} ms", duration.toMillis());

        startTime = Instant.now();
        createConnectionWithVehicles(vehicle, surroundingScooters, config.getDistances().getMaxScooterDistanceMeters());
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating connections with surrounding vehicles: {} ms", duration.toMillis());
    }

    private void createCarConnections(LandVehicle car) {
        var functionStartTime = Instant.now();
        var startTime = Instant.now();
        log.debug("Creating connections for car: {}", car.getId());
        if (config.getDistances().getMaxCarDistanceKms() != null) {
            var otherVehicles = vehicleDBService.findLandVehicleWithOneLevelConnectionNearLocation(car.getLocation(),
                    config.getDistances().getMaxCarDistanceKms());
            log.info("Getting surrounding vehicles took: {} ms", Duration.between(startTime, Instant.now()).toMillis());
            startTime = Instant.now();
            createConnectionWithVehicles(car, otherVehicles, config.getDistances().getMaxCarDistanceMeters());
        }
        log.debug("    Creating connections with surrounding vehicles took: {} ms",
                Duration.between(startTime, Instant.now()).toMillis());
        startTime = Instant.now();
        createConnectionWithPorts(car, config.getDistances().getMaxCarDistanceKms());
        log.debug("    Creating connections with ports took: {} ms",
                Duration.between(startTime, Instant.now()).toMillis());
        startTime = Instant.now();
        createConnectionWithBusStops(car, config.getDistances().getMaxCarDistanceKms());
        log.debug("    Creating connections with busStops took: {} ms",
                Duration.between(startTime, Instant.now()).toMillis());
        startTime = Instant.now();
        vehicleDBService.saveAndGet(car);
        log.debug("    Saving vehicle: {} ms", Duration.between(startTime, Instant.now()).toMillis());
        var duration = Duration.between(functionStartTime, Instant.now());
        log.debug("    Creating car connection: {} ms", duration.toMillis());
    }

    private void createPortConnections(Port port, List<Port> ports) {
        // Find all land vehicles around the port
        var surroundingVehicles = vehicleDBService.findLandVehicleWithOneLevelConnectionNearLocation(port.getLocation(),
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
        if (port.getParkedVehicles().isEmpty()) {
            port = connectPortWithOtherPorts(port, ports);
        }
        pointOfInterestService.saveAndGet(port);
    }

    public Port connectPortWithOtherPorts(Port p, List<Port> ports) {
        for (var otherPort : ports) {
            if (!otherPort.getId().equals(p.getId())) {
                p = pointOfInterestService.createConnectionFrom(p, otherPort, null);
            }
        }
        return p;
    }

    private void createBusStopConnections(BusStop busStop, List<BusStop> otherStops) {
        // Find all land vehicles around the bus stop
        var surroundingVehicles = vehicleDBService.findLandVehicleWithOneLevelConnectionNearLocation(
                busStop.getLocation(), config.getDistances().getMaxWalkingDistanceKms());

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

        // Calculate connections for all the ports
        var ports = pointOfInterestService.getAllPortsWithOneLevelConnection();
        var duration = Duration.between(startTime, Instant.now());
        log.debug("Getting ports: {} ms", duration.toMillis());

        startTime = Instant.now();
        for (var b : ports) {
            createPortConnections(b, ports);
        }
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating ports connections took: {} ms", duration.toMillis());

        startTime = Instant.now();
        var busStops = pointOfInterestService.getAllBusStopsWithOneLevelConnection();
        duration = Duration.between(startTime, Instant.now());
        log.debug("Getting bus stops: {} ms", duration.toMillis());

        startTime = Instant.now();
        for (var b : busStops) {
            createBusStopConnections(b, busStops);
        }
        duration = Duration.between(startTime, Instant.now());
        log.debug("Creating bus stop connections took: {} ms", duration.toMillis());

        // First we calculate connections for all the cars
        startTime = Instant.now();
        var vehicles = vehicleDBService.findAllLandVehicles(LandVehicle.class);
        duration = Duration.between(startTime, Instant.now());
        log.debug("Getting all vehicles took: {} ms", duration.toMillis());

        for (var startVehicle : vehicles) {
            createVehicleConnections(startVehicle);
        }
    }

    public void createVehicleConnections(LandVehicle vehicle) {
        if (vehicle.getType().equals(VehicleType.SCOOTER)) {
            createScooterConnections(vehicle);
        } else {
            createCarConnections(vehicle);
        }
    }

    public void graphDestruction() {
        connectionService.deleteAllConnections();
    }

}
