package gr.iccs.smart.mobility.pointsofinterest;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.iccs.smart.mobility.config.DataFileConfig;
import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.database.DatabaseService;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.scenario.RandomScenario;
import gr.iccs.smart.mobility.scenario.ScenarioDTO;
import gr.iccs.smart.mobility.scenario.ScenarioException;
import gr.iccs.smart.mobility.util.ResourceReader;
import gr.iccs.smart.mobility.util.RetryWithBackoff;
import gr.iccs.smart.mobility.vehicle.Vehicle;

@Service
public class PointOfInterestService {

    private DataFileConfig dataFileConfig;
    private ResourceReader resourceReader;
    private PointOfInterestRepository pointOfInterestRepository;
    private ConnectionService connectionService;
    private DatabaseService databaseService;
    private TransportationPropertiesConfig transportPropertiesConfig;
    private Neo4jTemplate neo4jTemplate;

    PointOfInterestService(DataFileConfig dataFileConfig, ResourceReader resourceReader,
            PointOfInterestRepository pointOfInterestRepository, ConnectionService connectionService,
            DatabaseService databaseService, TransportationPropertiesConfig transportPropertiesConfig,
            Neo4jTemplate neo4jTemplate) {
        this.dataFileConfig = dataFileConfig;
        this.resourceReader = resourceReader;
        this.pointOfInterestRepository = pointOfInterestRepository;
        this.connectionService = connectionService;
        this.databaseService = databaseService;
        this.transportPropertiesConfig = transportPropertiesConfig;
        this.neo4jTemplate = neo4jTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(PointOfInterestService.class);

    public List<PointOfInterest> getAll() {
        return pointOfInterestRepository.findAll();
    }

    public List<Port> getAllPorts() {
        return pointOfInterestRepository.findAllPorts();
    }

    public List<Port> getAllPortsWithOneLevelConnection() {
        return pointOfInterestRepository.getAllPortsByOneLevelConnection();
    }

    public List<BusStop> getAllBusStopsWithOneLevelConnection() {
        return pointOfInterestRepository.getAllBusStopsByOneLevelConnection();
    }

    public Integer countPorts() {
        return pointOfInterestRepository.countPorts();
    }

    public Integer countBusStops() {
        return pointOfInterestRepository.countBusStops();
    }

    public Optional<PointOfInterest> getByID(String id) {
        return pointOfInterestRepository.findById(id);
    }

    public List<Port> getByLocationNear(Point location) {
        return pointOfInterestRepository.findByLocationNear(location);
    }

    public List<Port> getPortsByLocationNear(Point location, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return pointOfInterestRepository.findPortsByLocationNear(location, range);
    }

    public List<BusStop> getBusStopsByLocationNear(Point location, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return pointOfInterestRepository.findBusStopsByLocationNear(location, range);
    }

    public List<PointOfInterest> getPOIByLocationNear(Point location, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return pointOfInterestRepository.findPOIByLocationNear(location, range);
    }

    public Optional<Port> getByExactLocation(Point location) {
        return pointOfInterestRepository.findByLocation(location);
    }

    public PointOfInterest create(PointOfInterest poi) {
        if (poi.getId() == null) {
            throw new IllegalArgumentException("Id cannot be null on creation");
        }
        var locationExists = pointOfInterestRepository.findByLocation(poi.getLocation());

        if (locationExists.isPresent()) {
            if (poi instanceof Port) {
                throw new IllegalArgumentException("There is already a port at the specified location");
            }
            throw new IllegalArgumentException("There is already a bus stop at the specified location");
        }
        return pointOfInterestRepository.save(poi);
    }

    public Port update(Port newPort) {
        var oldPort = pointOfInterestRepository.findById(newPort.getId());
        if (oldPort.isEmpty()) {
            throw new IllegalArgumentException("There is no boat stop to update");
        }
        return pointOfInterestRepository.save(newPort);
    }

    public void removeVehicle(String portId, String vehicleId) {
        pointOfInterestRepository.deleteParkedIn(portId, vehicleId);
    }

    public void removeVehicle(Port port, Vehicle v) {
        pointOfInterestRepository.deleteParkedIn(port.getId(), v.getId());
    }

    public Port createConnectionFrom(Port port, ReachableNode destination, Double maxDistanceMeters) {
        Connection connection;
        // If the reachable node is a port, we are using a boat.
        if (destination instanceof Port) {
            Double distance = databaseService.distance(port.getLocation(), destination.getLocation());
            Double time = distance / transportPropertiesConfig.getSpeeds().getBoatSpeedMetersPerSecond();
            connection = connectionService.createConnection(destination, distance, time);
        } else {
            // If it's anything else, the user will have to walk to the reachable node.
            connection = connectionService.generateConnection(port, destination);
            // We don't want to create connections that are actually longer than the walking
            // distance.
            if (connection.getDistance() > maxDistanceMeters) {
                return port;
            }
        }

        port.addConnection(connection);
        return port;
    }

    public BusStop createConnectionFrom(BusStop busStop, ReachableNode destination, Double maxDistanceMeters) {
        Connection connection;
        if (destination instanceof BusStop) {
            Double distance = databaseService.distance(busStop.getLocation(), destination.getLocation());
            Double time = distance;
            connection = connectionService.createConnection(destination, distance, time);
        } else {
            connection = connectionService.generateConnection(busStop, destination);
            if (connection.getDistance() > maxDistanceMeters) {
                return busStop;
            }
        }
        busStop.addConnection(connection);
        return busStop;
    }

    public Port getPortOfVehicle(String vehicleID) {
        return pointOfInterestRepository.getPortOfVehicle(vehicleID);
    }

    public void save(PointOfInterest pointOfInterest) {
        if (pointOfInterest instanceof Port) {
            neo4jTemplate.saveAs(pointOfInterest, PortWithOneLevelConnection.class);
        } else {
            neo4jTemplate.saveAs(pointOfInterest, BusStopWithOneLevelConnection.class);
        }
    }

    public Port saveAndGet(Port port) {
        return RetryWithBackoff.retryWithBackoff(() -> {
            neo4jTemplate.saveAs(port, PortWithOneLevelConnection.class);
            // We need to update the boat stop to get the new connection info
            return pointOfInterestRepository.getOnePortByOneLevelConnection(port.getId());
        }, 5, 1000, 2.0);
    }

    public Port getPortByOneLevelConnection(String portID) {
        return pointOfInterestRepository.getOnePortByOneLevelConnection(portID);
    }

    public BusStop saveAndGet(BusStop busStop) {
        neo4jTemplate.saveAs(busStop, BusStopWithOneLevelConnection.class);
        // We need to update the boat stop to get the new connection info
        return pointOfInterestRepository.getOneBusStopByOneLevelConnection(busStop.getId());
    }

    private void createRandomPorts(Integer n) {
        if (n > IstanbulLocations.coastLocations.size()) {
            throw new ScenarioException("Cannot create more ports than the number of coast locations");
        }
        // TODO: Improve this to create ports in random locations
        for (int i = 0; i < n; i++) {
            var port = new Port("port_" + i, "Port " + i, IstanbulLocations.coastLocations.get(i).toPoint(), null,
                    null);
            create(port);
        }
    }

    public PortWrapper creatPortWrapperFromFile() {
        String filePath = dataFileConfig.getPortLocations();
        try {
            ObjectMapper mapper = new ObjectMapper();
            var stream = resourceReader.readResource(filePath);
            return mapper.readValue(stream, PortWrapper.class);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                log.warn("File {} not found, stopping...", filePath);
            }
            throw new RuntimeException(e);
        }
    }

    public void createPortScenario(RandomScenario randomScenario, ScenarioDTO scenario) {
        if (randomScenario.randomize().booleanValue()) {
            createRandomPorts(randomScenario.ports());
            return;
        }

        List<PortDTO> ports = null;
        if (scenario == null) {
            ports = creatPortWrapperFromFile().getPorts();
        } else {
            ports = scenario.ports();
        }

        if (ports == null) {
            return;
        }

        for (var p : ports) {
            create(p.toPort());
        }
    }

    private void createRandomBusStops(Integer n) {
        for (int i = 0; i < n; i++) {
            var newLocation = IstanbulLocations.randomLandLocation();
            var busStop = new BusStop("bus_stop_" + i, "Bus Stop " + i, newLocation.toPoint(), null);
            create(busStop);
        }
    }

    public void createBusStopScenario(RandomScenario randomScenario, List<BusStopDTO> busStops) {
        if (randomScenario.randomize()) {
            createRandomBusStops(randomScenario.busStops());
            return;
        }

        if (busStops == null) {
            return;
        }

        for (var b : busStops) {
            create(b.toBusStop());
        }
    }
}
