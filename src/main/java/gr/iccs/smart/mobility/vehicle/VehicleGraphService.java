package gr.iccs.smart.mobility.vehicle;

import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.graph.GraphService;
import gr.iccs.smart.mobility.pointsOfInterest.BusStop;
import gr.iccs.smart.mobility.pointsOfInterest.PointOfInterestService;
import gr.iccs.smart.mobility.pointsOfInterest.Port;

@Service
public class VehicleGraphService {
    private final VehicleService vehicleService;
    private final PointOfInterestService pointOfInterestService;
    private final GraphService graphService;
    private final TransportationPropertiesConfig config;
    private final ExecutorService executorService;

    VehicleGraphService(VehicleService vehicleService,
            PointOfInterestService pointOfInterestService,
            GraphService graphService,
            TransportationPropertiesConfig config,
            ExecutorService executorService) {
        this.vehicleService = vehicleService;
        this.pointOfInterestService = pointOfInterestService;
        this.graphService = graphService;
        this.config = config;
        this.executorService = executorService;
    }

    public void addVehicleToGraphAsync(Vehicle v) {
        if (v.getStatus() == VehicleStatus.CREATING) {
            executorService.submit(() -> {
                addVehicleToGraph(v);
                v.setStatus(VehicleStatus.IDLE);
                vehicleService.saveAndGet(v);
            });
        }
    }

    public void removeVehicleFromGraph(Vehicle v) {
        if (v.isLandVehicle()) {
            vehicleService.deleteAllConnectionsOfLandVehicle(v.getId());
        } else {
            // If it's a boat, we have to remove it from the port and then check if
            // there are any boats remaining on that port.
            // If not, we remove all outgoing relations.
            var port = pointOfInterestService.getPortOfVehicle(v.getId());
            port.getParkedVehicles().removeIf(vehicle -> vehicle.getId().equals(v.getId()));

            if (port.getParkedVehicles().isEmpty()) {
                port.getConnections().clear();
            }
            pointOfInterestService.saveAndGet(port);
        }
    }

    private void addLandVehicleToGraph(LandVehicle v) {
        // Incoming relationships
        // Get all relationships with ports and bus stops in walking distance
        var poi = pointOfInterestService.getPOIByLocationNear(v.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());
        for (var p : poi) {
            if (p instanceof Port port) {
                pointOfInterestService.createConnectionFrom(port, v,
                        config.getDistances().getMaxWalkingDistanceMeters());

            } else if (p instanceof BusStop busStop) {
                pointOfInterestService.createConnectionFrom(busStop, v,
                        config.getDistances().getMaxWalkingDistanceMeters());
            }
            pointOfInterestService.save(p);
        }

        // Get all relationships with scooters in scooter distance
        var maxScooterDistance = config.getDistances().getMaxScooterDistanceMeters();
        var scooters = vehicleService.findVehicleByTypeAndLocationAround(VehicleType.SCOOTER, v.getLocation(),
                maxScooterDistance);
        for (var scooter : scooters) {
            if (scooter.getId().equals(v.getId())) {
                continue; // Skip the same vehicle
            }
            LandVehicle s = vehicleService.createConnectionTo((LandVehicle) scooter, (ReachableNode) v,
                    maxScooterDistance);
            vehicleService.saveAndGet(s);
        }

        // Get all relationships with cars in car distance
        var maxCarDistance = config.getDistances().getMaxCarDistanceMeters();
        var cars = vehicleService.findVehicleByTypeAndLocationAround(VehicleType.CAR, v.getLocation(),
                maxCarDistance);
        for (var car : cars) {
            if (car.getId().equals(v.getId())) {
                continue; // Skip the same vehicle
            }
            LandVehicle c = vehicleService.createConnectionTo((LandVehicle) car, (ReachableNode) v,
                    maxCarDistance);
            vehicleService.saveAndGet(c);
        }

        // Outgoing relationships
        graphService.createVehicleConnections(v);

    }

    public void addVehicleToGraph(Vehicle v) {
        if (v.isLandVehicle()) {
            addLandVehicleToGraph((LandVehicle) v);
        } else {
            // We have to add the boat to the port. If the port already has
            // boats, there is nothing else to do. Otherwise, we need to connect
            // the port with the other ports.
            var port = pointOfInterestService.getPortOfVehicle(v.getId());
            if (port.getParkedVehicles().isEmpty()) {
                var ports = pointOfInterestService.getAllPortsWithOneLevelConnection();
                graphService.connectPortWithOtherPorts(port, ports);
            }
            port.getParkedVehicles().add(v);
            pointOfInterestService.saveAndGet(port);
        }
    }

}
