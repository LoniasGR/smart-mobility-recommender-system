package gr.iccs.smart.mobility.graph;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.MovementPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStop;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStopService;
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
    private BoatStopService boatStopService;

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

    private LandVehicle createConnectionWithBoatStops(LandVehicle vehicle, Long range) {
        List<BoatStop> boatStops;
        if (range == null) {
            boatStops = boatStopService.getAllWithOneLevelConnection();
        } else {
            boatStops = boatStopService.getByLocationNear(vehicle.getLocation(), range);
        }
        for (var b : boatStops) {
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
        scooter = createConnectionWithBoatStops(scooter, maxSooterDistance);
    }

    private void createCarConnections(LandVehicle car) {
        // var otherVehicles =
        // vehicleService.getAllLandVehiclesWithOneLevelConnection();
        // createConnectionWithVehicles(car, otherVehicles);
        createConnectionWithBoatStops(car, null);
    }

    private void createBoatStopConnections(BoatStop boatStop, List<BoatStop> boatStops) {
        var surroundingVehicles = vehicleService.findLandVehicleWithOneLevelConnectionNearLocation(
                boatStop.getLocation(),
                config.getMaxWalkingDistance());

        for (var v : surroundingVehicles) {
            boatStop = boatStopService.createConnectionTo(boatStop, v);
        }

        // TODO: Make this more complex
        for (var bb : boatStops) {
            if (!bb.getId().equals(boatStop.getId()))
                boatStop = boatStopService.createConnectionTo(boatStop, bb);
        }
    }

    public void graphPreCalculation() {
        var boatStops = boatStopService.getAllWithOneLevelConnection();
        for (var b : boatStops) {
            createBoatStopConnections(b, boatStops);
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
