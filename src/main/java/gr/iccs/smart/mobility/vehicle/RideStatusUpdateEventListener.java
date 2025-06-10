package gr.iccs.smart.mobility.vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.graph.GraphService;
import gr.iccs.smart.mobility.pointsOfInterest.BusStop;
import gr.iccs.smart.mobility.pointsOfInterest.PointOfInterestService;
import gr.iccs.smart.mobility.pointsOfInterest.Port;
import gr.iccs.smart.mobility.usage.RideStatusUpdateEvent;
import gr.iccs.smart.mobility.usage.UseStatus;

@Component
public class RideStatusUpdateEventListener implements ApplicationListener<RideStatusUpdateEvent> {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PointOfInterestService pointOfInterestService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private TransportationPropertiesConfig config;

    @Override
    public void onApplicationEvent(RideStatusUpdateEvent event) {
        var useInfo = event.getUseInfo();
        VehicleStatus vehicleStatus;

        // We first need to do a pass on the vehicle status
        if (useInfo.status().equals(UseStatus.ACTIVE)) {
            vehicleStatus = VehicleStatus.IN_USE;
        } else {
            vehicleStatus = VehicleStatus.IDLE;
        }

        var vehicleInfo = new VehicleInfoDTO(null,
                useInfo.location().latitude(),
                useInfo.location().longitude(),
                vehicleStatus);

        vehicleService.updateVehicleStatus(event.getVehicleId(), vehicleInfo);

        var v = vehicleService.getByIdNoConnections(event.getVehicleId());

        // If the vehicle is in use, we have to remove it from the graph
        if (useInfo.status().equals(UseStatus.ACTIVE)) {
            if (v.isLandVehicle()) {
                vehicleService.deleteAllConnectionsOfLandVehicle(event.getVehicleId());
            } else {
                // If it's a boat, we have to remove it from the port and then check if
                // there are any boats remaining on that port.
                // If not, we remove all outgoing relations.
                var port = pointOfInterestService.getPortOfVehicle(event.getVehicleId());
                port.getParkedVehicles().removeIf(vehicle -> vehicle.getId().equals(v.getId()));

                if (port.getParkedVehicles().size() < 1) {
                    port.getConnections().clear();
                }
                pointOfInterestService.saveAndGet(port);
            }
        } else {
            // Here we should be adding the new relationships to the database
            // This needs to include incoming relationships and outgoing ones.
            if (v.isLandVehicle()) {
                // Incoming relationships
                // Get all relationships with ports and bus stops in walking distance
                var poi = pointOfInterestService.getPOIByLocationNear(v.getLocation(),
                        config.getDistances().getMaxWalkingDistanceKms());
                for (var p : poi) {
                    if (p instanceof Port) {
                        pointOfInterestService.createConnectionFrom((Port) p, (LandVehicle) v,
                                config.getDistances().getMaxWalkingDistanceMeters());
                        
                    } else if (p instanceof BusStop) {
                        pointOfInterestService.createConnectionFrom((BusStop) p, (LandVehicle) v,
                                config.getDistances().getMaxWalkingDistanceMeters());
                    }
                    pointOfInterestService.save(p);
                }

                // Get all relationships with scooters in scooter distance
                var maxScooterDistance = config.getDistances().getMaxScooterDistanceMeters();
                var scooters = vehicleService.findVehicleByTypeAndLocationAround(VehicleType.SCOOTER,v.getLocation(),
                        maxScooterDistance);
                for (var scooter : scooters) {
                    if (scooter.getId().equals(v.getId())) {
                        continue; // Skip the same vehicle
                    }
                    LandVehicle s = vehicleService.createConnectionTo((LandVehicle) scooter,(ReachableNode) v, maxScooterDistance);
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
                    LandVehicle c = vehicleService.createConnectionTo((LandVehicle) car, (ReachableNode) v, maxCarDistance);
                    vehicleService.saveAndGet(c);
                }

                // Outgoing relationships
                graphService.createVehicleConnections((LandVehicle) v);
            } else {
                // We have to add the boat to the port. If the port already has
                // boats, there is nothing else to do. Otherwise, we need to connect
                // the port with the other ports.
                var port = pointOfInterestService.getPortOfVehicle(event.getVehicleId());
                if (port.getParkedVehicles().size() < 1) {
                    var ports = pointOfInterestService.getAllPortsWithOneLevelConnection();
                    graphService.connectPortWithOtherPorts(port, ports);
                }
                port.getParkedVehicles().add(v);
                pointOfInterestService.saveAndGet(port);
            }
        }
    }
}
