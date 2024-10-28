package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.neo4j.driver.types.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.connection.Connection;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.database.DatabaseService;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.vehicle.Vehicle;

@Service
public class BoatStopService {

    @Autowired
    private BoatStopRepository boatStopRepository;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    public List<BoatStop> getAll() {
        return boatStopRepository.findAll();
    }

    public List<BoatStop> getAllWithOneLevelConnection() {
        return boatStopRepository.getAllByOneLevelConnection();
    }

    public Optional<BoatStop> getByID(UUID id) {
        return boatStopRepository.findById(id);
    }

    public List<BoatStop> getByLocationNear(Point location) {
        return boatStopRepository.findByLocationNear(location);
    }

    public List<BoatStop> getByLocationNear(Point location, Long distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return boatStopRepository.findByLocationNear(location, range);
    }

    public Optional<BoatStop> getByExactLocation(Point location) {
        return boatStopRepository.findByLocation(location);
    }

    public BoatStop create(BoatStop boatStop) {
        var locationExists = boatStopRepository.findByLocation(boatStop.getLocation());

        if (locationExists.isPresent()) {
            throw new IllegalArgumentException("There is already a boat stop at the specified location");
        }
        return boatStopRepository.save(boatStop);
    }

    public BoatStop update(BoatStop newBoatStop) {
        var oldBoatStop = boatStopRepository.findById(newBoatStop.getId());
        if (oldBoatStop.isEmpty()) {
            throw new IllegalArgumentException("There is no boat stop to update");
        }
        return boatStopRepository.save(newBoatStop);
    }

    public void removeVehicle(BoatStop boatStop, Vehicle v) {
        boatStopRepository.deleteParkedIn(boatStop.getId(), v.getId());
    }

    public BoatStop createConnectionTo(BoatStop boatStop, ReachableNode destination) {
        Connection connection;
        if (!(destination instanceof BoatStop)) {
            connection = connectionService.generateConnection(boatStop, destination);
        } else {
            Double distance = databaseService.distance(boatStop.getLocation(), destination.getLocation());
            connection = connectionService.createConnection(destination, distance, distance);
        }

        boatStop.addConnection(connection);
        neo4jTemplate.saveAs(boatStop, BoatStopWithOneLevelConnection.class);

        // We need to update the boat stop to get the new connection info
        return boatStopRepository.getOneByOneLevelConnection(boatStop.getId());
    }

    public void createBoatStopScenario() {
        for (int i = 0; i < IstanbulLocations.coastLocations.size(); i++) {
            var boardStop = new BoatStop(UUID.randomUUID(), IstanbulLocations.coastLocations.get(i).toPoint(), null);
            create(boardStop);
        }
    }
}
