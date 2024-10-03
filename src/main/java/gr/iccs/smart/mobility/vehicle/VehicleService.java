package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStop;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStopService;

@Service
public class VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private static final Random RANDOM = new Random();
    private final VehicleRepository vehicleRepository;
    private final BoatStopService boatStopService;
    private final ConnectionService connectionService;
    private final Neo4jTemplate neo4jTemplate;

    public VehicleService(VehicleRepository vehicleRepository, BoatStopService boatStopService,
            ConnectionService connectionService, Neo4jTemplate neo4jTemplate) {
        this.vehicleRepository = vehicleRepository;
        this.boatStopService = boatStopService;
        this.connectionService = connectionService;
        this.neo4jTemplate = neo4jTemplate;
    }

    public List<Vehicle> getAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle create(Vehicle vehicle) {
        if (vehicle.getType() == null) {
            throw new BadVehicleRequest("vehicle type is missing");
        }

        if (vehicleRepository.existsById(vehicle.getId())) {
            throw new BadVehicleRequest("Vehicle already exists");
        }
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getById(UUID id) {
        var vehicle = vehicleRepository.findById(id);
        if (vehicle.isPresent()) {
            return vehicle.get();
        }
        throw new VehicleNotFoundException();
    }

    public Vehicle updateVehicleStatus(UUID id, VehicleInfoDTO vehicleInfoDTO) {
        var oldVehicle = vehicleRepository.findById(id);
        if (oldVehicle.isEmpty()) {
            throw new VehicleNotFoundException();
        }
        var vehicle = oldVehicle.get();
        var newLocation = Values.point(4326, vehicleInfoDTO.latitude(), vehicleInfoDTO.longitude()).asPoint();
        var boatStops = boatStopService.getAll();
        validateLocation(newLocation, boatStops, vehicle.getType());

        if (vehicle.getType() == VehicleType.SEA_VESSEL) {
            updateRelatedBoatStops(newLocation, boatStops, vehicle);
        }
        vehicle.setLocation(newLocation);
        vehicle.setStatus(vehicleInfoDTO.status());
        if (vehicleInfoDTO.battery() != null) {
            vehicle.setBattery(vehicleInfoDTO.battery());
        }
        return vehicleRepository.save(vehicle);
    }

    public void createConnectionTo(LandVehicle vehicle, ReachableNode destination) {
        var connection = connectionService.generateConnection(vehicle, destination);
        vehicle.getConnections().add(connection);
        neo4jTemplate.saveAs(vehicle, LandVehicleWithOneLevelLink.class);
    }

    private void validateLocation(Point newLocation, List<BoatStop> boatStops, VehicleType vehicleType) {
        var isSeaLocation = LocationDTO
                .istanbulLocation(newLocation) == IstanbulLocations.IstanbulLocationDescription.SEA;
        var isCoastLocation = boatStops.stream().anyMatch(bs -> bs.getLocation().equals(newLocation));

        // Check if the location provided is valid for a sea vessel
        if (vehicleType == VehicleType.SEA_VESSEL && !(isSeaLocation || isCoastLocation)) {
            throw new BadVehicleRequest("A sea vessel cannot go in the land");
        }

        // Check if location provided is valid for other vehicles
        if (vehicleType != VehicleType.SEA_VESSEL && isSeaLocation) {
            throw new BadVehicleRequest("A land vehicle cannot go in the sea");
        }
    }

    private void updateRelatedBoatStops(Point newLocation, List<BoatStop> boatStops, Vehicle vehicle) {
        var coastLocation = boatStops.stream().filter(bs -> bs.getLocation().equals(newLocation)).findFirst();

        // If the sea vessel is parked in a boat stop, add it to the list of boats in
        // the stop
        if (coastLocation.isPresent() && !newLocation.equals(vehicle.getLocation())) {
            coastLocation.get().getParkedVehicles().add(vehicle);
            boatStopService.update(coastLocation.get());
        }
        // Otherwise remove it if it's leaving a boat stop
        else {
            boatStopService
                    .getByExactLocation(vehicle.getLocation())
                    .ifPresent(boatStop -> boatStopService.removeVehicle(boatStop, vehicle));
        }
    }

    public FeatureCollection createGeoJSON() {
        var vehicles = getAll();
        FeatureCollection geoJSON = new FeatureCollection();

        for (Vehicle v : vehicles) {
            geoJSON.getFeatures().add(GeoJSONUtils.createVehicleFeature(v));
        }
        return geoJSON;
    }

    public <T> List<T> getAllLandVehicles(Class<T> type) {
        return vehicleRepository.getAllLandVehicles(type);
    }

    public List<LandVehicle> getAllLandVehiclesWithOneLevelConnection() {
        return vehicleRepository.findAllWithOneLevelConnection();
    }

    public List<LandVehicle> findLandVehicleWithOneLevelConnectionNearLocation(Point point, Long max) {
        return vehicleRepository.findLandVechicleWithOneLevelConnectionByLocationNear(point, max);
    }

    public <T> List<T> findLandVehicleNearLocation(Class<T> type, Point point, Long max) {
        return vehicleRepository.findLandVesselsByLocationNear(point, max, type);
    }

    public List<LandVehicle> findLandVesselsByLocationAround(Point point, Distance distance, Integer max) {
        return vehicleRepository.findLandVesselsByLocationAround(point, distance.getValue(), max);
    }

    public List<Vehicle> findVehicleByTypeAndLocationAround(VehicleType type, Point point, Distance distance,
            Integer max) {
        return vehicleRepository.findVehicleByTypeAndLocationAround(type.name(), point, distance.getValue(), max);
    }

    public List<Vehicle> findVehicleByTypeAndLocationNear(VehicleType type, Point point, Integer max) {
        return vehicleRepository.findVehicleByTypeAndLocationNear(type.name(), point, max);
    }

    public List<Vehicle> findSeaVesselsParkedInBoatStop(UUID uuid) {
        return vehicleRepository.findSeaVesselsParkedInBoatStop(uuid);
    }

    public List<Vehicle> findNearLocation(Point point, Distance maxDistance) {
        return vehicleRepository.findByLocationNear(point, maxDistance);
    }

    public void createScenarioVehicles() {
        for (int i = 0; i < 5; i++) {
            var vehicle = new LandVehicle(UUID.randomUUID(), VehicleType.CAR, null);
            create(vehicle);
        }
        for (int i = 0; i < 5; i++) {
            var vehicle = new LandVehicle(UUID.randomUUID(), VehicleType.SCOOTER, null);
            create(vehicle);
        }
        for (int i = 0; i < 10; i++) {
            var vehicle = new Boat(UUID.randomUUID(), VehicleType.SEA_VESSEL, 10);
            create(vehicle);
        }
    }

    public void createScenarioLocations() {
        var vehicles = getAll();
        for (Vehicle v : vehicles) {
            LocationDTO newLocation;
            if (v.getType() == VehicleType.SEA_VESSEL) {
                newLocation = IstanbulLocations.randomCoastLocation();
            } else {
                newLocation = IstanbulLocations.randomLandLocation();
            }
            VehicleInfoDTO vehicleInfo = new VehicleInfoDTO(
                    RANDOM.nextFloat(100),
                    newLocation.latitude(),
                    newLocation.longitude(),
                    VehicleStatus.IDLE);
            updateVehicleStatus(v.getId(), vehicleInfo);
        }
    }

    public Double distance(Point p1, Point p2) {
        return vehicleRepository.calculateDistance(p1, p2);
    }

    public Double distance(LocationDTO loc1, LocationDTO loc2) {
        return vehicleRepository.calculateDistance(loc1.toPoint(), loc2.toPoint());
    }
}
