package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

@Service
public class VehicleDBService {
    private final VehicleRepository vehicleRepository;
    private final Neo4jTemplate neo4jTemplate;

    public VehicleDBService(VehicleRepository vehicleRepository, Neo4jTemplate neo4jTemplate) {
        this.vehicleRepository = vehicleRepository;
        this.neo4jTemplate = neo4jTemplate;
    }

    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    protected void validateVehicle(VehicleType type, String vehicleId) {
        if (type == null) {
            throw new BadVehicleRequest("vehicle type is missing");
        }

        if (vehicleId == null) {
            throw new BadVehicleRequest("Vehicle id is missing");
        }

        if (vehicleRepository.existsById(vehicleId)) {
            throw new BadVehicleRequest("Vehicle already exists");
        }
    }

    public List<VehicleDTO> getAll() {
        return vehicleRepository.findAllVehiclesNoConnections();
    }

    public Vehicle getById(String id) {
        var vehicle = vehicleRepository.findById(id);
        if (vehicle.isPresent()) {
            return vehicle.get();
        }
        throw new VehicleNotFoundException();
    }

    public Vehicle getByIdNoConnections(String id) {
        var vehicle = vehicleRepository.findNoConnectionsById(id);
        if (vehicle.isPresent()) {
            return vehicle.get();
        }
        throw new VehicleNotFoundException();
    }

    public Vehicle getByIdOneLevelConnections(String id) {
        var vehicle = vehicleRepository.findOneLevelConnectionsById(id);
        if (vehicle.isPresent()) {
            return vehicle.get();
        }
        throw new VehicleNotFoundException();
    }

    public LandVehicle getLandVehicleByIdNoConnections(String id) {
        var vehicle = vehicleRepository.findLandVehicleWithNoConnections(id);
        if (vehicle.isPresent()) {
            return vehicle.get();
        }
        throw new VehicleNotFoundException();
    }

    public Vehicle createVehicle(Vehicle v) {
        validateVehicle(v.getType(), v.getId());
        return vehicleRepository.save(v);
    }

    public <T> List<T> findAllLandVehicles(Class<T> type) {
        return vehicleRepository.getAllLandVehicles(type);
    }

    public List<LandVehicle> findAllLandVehiclesWithOneLevelConnection() {
        return vehicleRepository.findAllLandVehiclesWithOneLevelConnection();
    }

    public List<LandVehicle> findLandVehicleWithOneLevelConnectionNearLocation(Point point, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return vehicleRepository.findLandVechicleWithOneLevelConnectionByLocationAround(point, range);
    }

    public List<LandVehicle> findScooterNolConnectionNearLocation(Point point, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return vehicleRepository.findScooterNoConnectionByLocationAround(point, range);
    }

    public List<LandVehicle> findLandVehicleNoConnectionByNearLocation(Point point, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return vehicleRepository.findLandVehicleNoConnectionByLocationAround(point, range);
    }

    public List<LandVehicle> findLandVesselsByLocationAround(Point point, Distance distance, Integer max) {
        return vehicleRepository.findLandVehiclesByLocationAround(point, distance.getValue(), max);
    }

    public List<Vehicle> findVehicleByTypeAndLocationAround(VehicleType type, Point point, Double distance) {
        return vehicleRepository.findVehicleByTypeAndLocationAround(type.name(), point, distance);
    }

    public List<Vehicle> findVehicleByTypeAndLocationNear(VehicleType type, Point point, Integer max) {
        return vehicleRepository.findVehicleByTypeAndLocationNear(type.name(), point, max);
    }

    public List<Vehicle> findSeaVesselsParkedInPort(String id) {
        return vehicleRepository.findSeaVesselsParkedInPort(id);
    }

    public List<Vehicle> findNearLocation(Point point, Distance maxDistance) {
        return vehicleRepository.findByLocationNear(point, maxDistance);
    }

    public void deleteAllConnectionsOfLandVehicle(String vehicleId) {
        vehicleRepository.deleteAllConnectionsOfLandVehicle(vehicleId);
    }

    public LandVehicle saveAndGet(LandVehicle vehicle) {
        neo4jTemplate.saveAs(vehicle, LandVehicleWithOneLevelLink.class);
        return vehicleRepository.findLandVehicleWithOneLevelConnection(vehicle.getId()).get();
    }
}
