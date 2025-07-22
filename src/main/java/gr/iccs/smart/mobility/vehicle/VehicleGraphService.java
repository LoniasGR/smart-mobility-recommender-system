package gr.iccs.smart.mobility.vehicle;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.graph.GraphService;
import gr.iccs.smart.mobility.pointsofinterest.BusStop;
import gr.iccs.smart.mobility.pointsofinterest.PointOfInterestService;
import gr.iccs.smart.mobility.pointsofinterest.Port;

@Service
public class VehicleGraphService {
    private static final Logger log = LoggerFactory.getLogger(VehicleGraphService.class);

    private final VehicleDBService vehicleDBService;
    private final VehicleUtilitiesService vehicleUtilitiesService;
    private final PointOfInterestService pointOfInterestService;
    private final GraphService graphService;
    private final TransportationPropertiesConfig config;
    private final ExecutorService executorService;

    VehicleGraphService(VehicleDBService vehicleDBService, PointOfInterestService pointOfInterestService,
            VehicleUtilitiesService vehicleUtilitiesService, GraphService graphService,
            TransportationPropertiesConfig config, ExecutorService executorService) {
        this.vehicleDBService = vehicleDBService;
        this.vehicleUtilitiesService = vehicleUtilitiesService;
        this.pointOfInterestService = pointOfInterestService;
        this.graphService = graphService;
        this.config = config;
        this.executorService = executorService;
    }

    public void addVehicleToGraphAsync(Vehicle v) {
        if (v.getStatus() == VehicleStatus.CREATING || v.getStatus() == VehicleStatus.IN_USE) {
            executorService.submit(() -> {
                try {
                    log.info("Processing vehicle {}", v.getId());
                    var newV = addVehicleToGraph(v);
                    newV.setStatus(VehicleStatus.IDLE);
                    vehicleDBService.save(newV);
                } catch (Exception e) {
                    log.error("Exception while processing vehicle {}", v.getId(), e);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void removeVehicleFromGraph(Vehicle v) {
        if (v.isLandVehicle()) {
            vehicleDBService.deleteAllConnectionsOfLandVehicle(v.getId());
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

    private Vehicle addLandVehicleToGraph(LandVehicle v) {
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
        var scooters = vehicleDBService.findVehicleByTypeAndLocationAround(VehicleType.SCOOTER, v.getLocation(),
                maxScooterDistance);
        for (var scooter : scooters) {
            if (scooter.getId().equals(v.getId())) {
                continue; // Skip the same vehicle
            }
            LandVehicle s = vehicleUtilitiesService.createConnectionTo((LandVehicle) scooter, (ReachableNode) v,
                    maxScooterDistance);
            vehicleDBService.saveAndGet(s);
        }

        // Get all relationships with cars in car distance
        var maxCarDistance = config.getDistances().getMaxCarDistanceMeters();
        var cars = vehicleDBService.findVehicleByTypeAndLocationAround(VehicleType.CAR, v.getLocation(),
                maxCarDistance);
        for (var car : cars) {
            if (car.getId().equals(v.getId())) {
                continue; // Skip the same vehicle
            }
            LandVehicle c = vehicleUtilitiesService.createConnectionTo((LandVehicle) car, (ReachableNode) v,
                    maxCarDistance);
            vehicleDBService.saveAndGet(c);
        }

        // Outgoing relationships
        return graphService.createVehicleConnections(v);

    }

    public Vehicle addVehicleToGraph(Vehicle v) {
        if (v.isLandVehicle()) {
            return addLandVehicleToGraph((LandVehicle) v);
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
            pointOfInterestService.save(port);
            return v;
        }
    }
}
