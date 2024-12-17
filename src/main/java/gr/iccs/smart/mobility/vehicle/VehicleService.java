package gr.iccs.smart.mobility.vehicle;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.neo4j.driver.Values;
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
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.pointsOfInterest.Port;
import gr.iccs.smart.mobility.pointsOfInterest.PortService;
import gr.iccs.smart.mobility.util.ResourceReader;

@Service
public class VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private static final Random RANDOM = new Random();

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PortService portService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    @Autowired
    private DataFileConfig dataFileConfig;

    @Autowired
    private ResourceReader resourceReader;

    public List<VehicleDTO> getAll() {
        return vehicleRepository.findAllVehiclesNoConnections();
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

    public Vehicle getById(String id) {
        var vehicle = vehicleRepository.findById(id);
        if (vehicle.isPresent()) {
            return vehicle.get();
        }
        throw new VehicleNotFoundException();
    }

    public Vehicle updateVehicleStatus(String id, VehicleInfoDTO vehicleInfoDTO) {
        var oldVehicle = vehicleRepository.findById(id);
        if (oldVehicle.isEmpty()) {
            throw new VehicleNotFoundException();
        }
        var vehicle = oldVehicle.get();
        var newLocation = Values.point(4326, vehicleInfoDTO.latitude(), vehicleInfoDTO.longitude()).asPoint();
        var ports = portService.getAll();
        validateLocation(newLocation, ports, vehicle.getType());

        if (vehicle.getType() == VehicleType.SEA_VESSEL) {
            updateRelatedPorts(newLocation, ports, vehicle);
        }
        vehicle.setLocation(newLocation);
        vehicle.setStatus(vehicleInfoDTO.status());
        if (vehicleInfoDTO.battery() != null) {
            vehicle.setBattery(vehicleInfoDTO.battery().level());
        }
        return vehicleRepository.save(vehicle);
    }

    public LandVehicle createConnectionTo(LandVehicle vehicle, ReachableNode destination) {
        var connection = connectionService.generateConnection(vehicle, destination);
        vehicle.addConnection(connection);
        neo4jTemplate.saveAs(vehicle, LandVehicleWithOneLevelLink.class);
        return vehicleRepository.findLandVehicleWithOneLevelConnection(vehicle.getId());
    }

    private void validateLocation(Point newLocation, List<Port> ports, VehicleType vehicleType) {
        var isSeaLocation = LocationDTO
                .istanbulLocation(newLocation) == IstanbulLocations.IstanbulLocationDescription.SEA;
        var isCoastLocation = ports.stream().anyMatch(bs -> bs.getLocation().equals(newLocation));

        // Check if the location provided is valid for a sea vessel
        if (vehicleType == VehicleType.SEA_VESSEL && !(isSeaLocation || isCoastLocation)) {
            throw new BadVehicleRequest("A sea vessel cannot go in the land");
        }

        // Check if location provided is valid for other vehicles
        if (vehicleType != VehicleType.SEA_VESSEL && isSeaLocation) {
            throw new BadVehicleRequest("A land vehicle cannot go in the sea");
        }
    }

    private void updateRelatedPorts(Point newLocation, List<Port> ports, Vehicle vehicle) {
        var coastLocation = ports.stream().filter(bs -> bs.getLocation().equals(newLocation)).findFirst();

        // If the sea vessel is parked in a boat stop, add it to the list of boats in
        // the stop
        if (coastLocation.isPresent() && !newLocation.equals(vehicle.getLocation())) {
            coastLocation.get().getParkedVehicles().add(vehicle);
            portService.update(coastLocation.get());
        }
        // Otherwise remove it if it's leaving a boat stop
        else {
            portService
                    .getByExactLocation(vehicle.getLocation())
                    .ifPresent(port -> portService.removeVehicle(port, vehicle));
        }
    }

    public FeatureCollection createGeoJSON() {
        FeatureCollection geoJSON = new FeatureCollection();
        geoJSON = addVehiclesToGeoJSON(geoJSON);
        return geoJSON;
    }

    public FeatureCollection addVehiclesToGeoJSON(FeatureCollection fc) {
        var vehicles = getAll();
        for (var v : vehicles) {
            fc.getFeatures().add(GeoJSONUtils.createVehicleFeature(v));
        }
        return fc;

    }

    public <T> List<T> getAllLandVehicles(Class<T> type) {
        return vehicleRepository.getAllLandVehicles(type);
    }

    public List<LandVehicle> getAllLandVehiclesWithOneLevelConnection() {
        return vehicleRepository.findAllLandVehiclesWithOneLevelConnection();
    }

    public List<LandVehicle> findLandVehicleWithOneLevelConnectionNearLocation(Point point, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return vehicleRepository.findLandVechicleWithOneLevelConnectionByLocationAround(point, range);
    }

    public List<LandVehicle> findLandVehicleNoConnectionByNearLocation(Point point, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return vehicleRepository.findLandVehicleNoConnectionByLocationAround(point, range);
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

    public List<Vehicle> findSeaVesselsParkedInPort(String id) {
        return vehicleRepository.findSeaVesselsParkedInPort(id);
    }

    public List<Vehicle> findNearLocation(Point point, Distance maxDistance) {
        return vehicleRepository.findByLocationNear(point, maxDistance);
    }

    private void createScenarioCars() {
        String filePath = dataFileConfig.getCarLocations();
        try {
            ObjectMapper mapper = new ObjectMapper();
            var stream = resourceReader.readResource(filePath);
            var cars = mapper.readValue(stream, CarWrapper.class);
            for (var p : cars.getCars()) {
                create(p.toCar());
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                log.warn("File %s not found, randomly generating Ports", filePath);
                var cars = createRandomCars().stream().map(c -> VehicleDTO.fromVehicle(c)).toList();
                createScenarioLocations(cars);
                return;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Car> createRandomCars() {
        List<Car> cars = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            var vehicle = new Car(UUID.randomUUID().toString(), VehicleType.CAR, true, null);
            cars.add((Car) create(vehicle));
        }
        return cars;
    }

    private List<Scooter> createRandomScooters() {
        List<Scooter> scooters = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            var vehicle = new Scooter(UUID.randomUUID().toString(), VehicleType.SCOOTER, true, null);
            scooters.add((Scooter) create(vehicle));
        }
        return scooters;
    }

    public List<Boat> createRandomBoats() {
        List<Boat> boats = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            var vehicle = new Boat(UUID.randomUUID().toString(), VehicleType.SEA_VESSEL, true, 10);
            boats.add((Boat) create(vehicle));
        }
        return boats;
    }

    public void createScenarioVehicles() {
        createScenarioCars();
        var scooters = createRandomScooters().stream().map(s -> VehicleDTO.fromVehicle(s)).toList();
        createScenarioLocations(scooters);
        var boats = createRandomBoats().stream().map(b -> VehicleDTO.fromVehicle(b)).toList();
        createScenarioLocations(boats);
    }

    public void createScenarioLocations(List<VehicleDTO> vehicles) {
        var ports = portService.getAll();
        for (var v : vehicles) {
            LocationDTO newLocation;
            if (v.type() == VehicleType.SEA_VESSEL) {
                newLocation = LocationDTO.fromGeographicPoint(ports.get(RANDOM.nextInt(ports.size())).getLocation());
            } else {
                newLocation = IstanbulLocations.randomLandLocation();
            }
            VehicleInfoDTO vehicleInfo = new VehicleInfoDTO(
                    new Battery(RANDOM.nextLong(100)),
                    newLocation.latitude(),
                    newLocation.longitude(),
                    VehicleStatus.IDLE);
            updateVehicleStatus(v.id(), vehicleInfo);
        }
    }
}
