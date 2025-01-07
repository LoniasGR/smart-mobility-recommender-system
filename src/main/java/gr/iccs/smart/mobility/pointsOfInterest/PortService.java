package gr.iccs.smart.mobility.pointsOfInterest;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.iccs.smart.mobility.config.DataFileConfig;
import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.database.DatabaseService;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.util.ResourceReader;
import gr.iccs.smart.mobility.vehicle.Vehicle;

@Service
public class PortService {

    @Autowired
    private DataFileConfig dataFileConfig;

    @Autowired
    private ResourceReader resourceReader;

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    private static final Logger log = LoggerFactory.getLogger(PortService.class);

    public List<Port> getAll() {
        return portRepository.findAll();
    }

    public List<Port> getAllWithOneLevelConnection() {
        return portRepository.getAllByOneLevelConnection();
    }

    public Optional<Port> getByID(String id) {
        return portRepository.findById(id);
    }

    public List<Port> getByLocationNear(Point location) {
        return portRepository.findByLocationNear(location);
    }

    public List<Port> getByLocationNear(Point location, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return portRepository.findByLocationNear(location, range);
    }

    public Optional<Port> getByExactLocation(Point location) {
        return portRepository.findByLocation(location);
    }

    public Port create(Port port) {
        var locationExists = portRepository.findByLocation(port.getLocation());

        if (locationExists.isPresent()) {
            throw new IllegalArgumentException("There is already a boat stop at the specified location");
        }
        return portRepository.save(port);
    }

    public Port update(Port newPort) {
        var oldPort = portRepository.findById(newPort.getId());
        if (oldPort.isEmpty()) {
            throw new IllegalArgumentException("There is no boat stop to update");
        }
        return portRepository.save(newPort);
    }

    public void removeVehicle(Port port, Vehicle v) {
        portRepository.deleteParkedIn(port.getId(), v.getId());
    }

    public Port createConnectionFrom(Port port, ReachableNode destination, Double maxDistanceMeters) {
        Connection connection;
        // If the reachable node is a port, we are using a boat.
        if (destination instanceof Port) {
            Double distance = databaseService.distance(port.getLocation(), destination.getLocation());
            connection = connectionService.createConnection(destination, distance, distance);
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
        neo4jTemplate.saveAs(port, PortWithOneLevelConnection.class);

        // We need to update the boat stop to get the new connection info
        return portRepository.getOneByOneLevelConnection(port.getId());
    }

    public void createRandomPorts() {
        for (int i = 0; i < IstanbulLocations.coastLocations.size(); i++) {
            var port = new Port("port_" + i, "Port " + i, IstanbulLocations.coastLocations.get(i).toPoint(), null);
            create(port);
        }
    }

    public void createPortScenario(Boolean randomize) {
        if (randomize) {
            createRandomPorts();
            return;
        }

        // We use a given file to create the random ports
        String filePath = dataFileConfig.getPortLocations();
        try {
            ObjectMapper mapper = new ObjectMapper();
            var stream = resourceReader.readResource(filePath);
            var portDTOs = mapper.readValue(stream, PortWrapper.class);
            for (var p : portDTOs.getPorts()) {
                create(p.toPort());
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                log.warn("File %s not found, stopping...", filePath);
            }
            throw new RuntimeException(e);
        }
    }
}
