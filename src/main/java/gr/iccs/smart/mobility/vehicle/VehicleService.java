package gr.iccs.smart.mobility.vehicle;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

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
import gr.iccs.smart.mobility.pointsOfInterest.PointOfInterestService;
import gr.iccs.smart.mobility.pointsOfInterest.Port;
import gr.iccs.smart.mobility.scenario.RandomScenario;
import gr.iccs.smart.mobility.scenario.ScenarioDTO;
import gr.iccs.smart.mobility.util.ResourceReader;

@Service
public class VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private static final Random RANDOM = new Random();

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PointOfInterestService pointOfInterestService;

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

        if (vehicle.getId() == null) {
            throw new BadVehicleRequest("Vehicle id is missing");
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
        var ports = pointOfInterestService.getAllPorts();
        validateLocation(newLocation, ports, vehicle.getType());

        if (vehicle.getType() == VehicleType.SEA_VESSEL) {
            addBoatToPort(newLocation, ports, vehicle);
        }
        vehicle.setLocation(newLocation);
        vehicle.setStatus(vehicleInfoDTO.status());
        if (vehicleInfoDTO.battery() != null) {
            vehicle.setBattery(vehicleInfoDTO.battery().level());
        }
        return vehicleRepository.save(vehicle);
    }

    public LandVehicle createConnectionTo(LandVehicle vehicle, ReachableNode destination, Double maxDistance) {
        var connection = connectionService.generateConnection(vehicle, destination);
        if (maxDistance != null && connection.getDistance() > maxDistance) {
            return vehicle;
        }
        vehicle.addConnection(connection);
        return vehicle;
    }

    public LandVehicle saveAndGet(LandVehicle vehicle) {
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

    private void assignRelatedPort(List<Port> ports, Vehicle vehicle) {
        var coastLocation = ports.stream().filter(bs -> bs.getLocation().equals(vehicle.getLocation())).findFirst();
        if (coastLocation.isPresent()) {
            coastLocation.get().getParkedVehicles().add(vehicle);
            pointOfInterestService.update(coastLocation.get());
        }
    }

    private void addBoatToPort(Point newLocation, List<Port> ports, Vehicle vehicle) {
        var coastLocation = ports.stream().filter(bs -> bs.getLocation().equals(newLocation)).findFirst();

        // If the sea vessel is parked in a boat stop, add it to the list of boats in
        // the stop
        if (coastLocation.isPresent() && !newLocation.equals(vehicle.getLocation())) {
            coastLocation.get().getParkedVehicles().add(vehicle);
            pointOfInterestService.update(coastLocation.get());
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

    public List<LandVehicle> findScooterNolConnectionNearLocation(Point point, Double distance) {
        var range = new Distance(distance, Metrics.KILOMETERS);
        return vehicleRepository.findScooterNoConnectionByLocationAround(point, range);
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

    public void deleteAllConnectionsOfLandVehicle(String vehicleId) {
        vehicleRepository.deleteAllConnectionsOfLandVehicle(vehicleId);
    }

    public CarWrapper createCarsFromResourceFile() {
        String filePath = dataFileConfig.getCarLocations();
        try {
            ObjectMapper mapper = new ObjectMapper();
            var stream = resourceReader.readResource(filePath);
            return mapper.readValue(stream, CarWrapper.class);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                log.warn("File %s not found, terminating...", filePath);
            }
            throw new RuntimeException(e);
        }
    }

    private void createRandomCars(Integer n) {
        for (int i = 0; i < n; i++) {
            var vehicle = new Car("car_" + i, VehicleType.CAR, true, null);
            var car = create(vehicle);
            createRandomVehicleInfo(car);
        }
    }

    public void createScenarioCars(RandomScenario randomScenario, ScenarioDTO scenario) {
        if (randomScenario.randomize()) {
            createRandomCars(randomScenario.cars());
            return;
        }

        List<CarDTO> cars = null;
        if (scenario == null) {
            cars = createCarsFromResourceFile().getCars();
        } else {
            cars = scenario.cars();
        }

        if (cars == null) {
            return;
        }

        for (var p : cars) {
            var car = p.toCar();
            car.setStatus(VehicleStatus.IDLE);
            create(car);
        }
    }

    public void createRandomScooters(Integer n) {
        for (int i = 0; i < n; i++) {
            var vehicle = new Scooter("scooter_" + i, VehicleType.SCOOTER, true, null);
            var scooter = create(vehicle);
            createRandomVehicleInfo(scooter);
        }
    }

    public void createScenarioScooters(RandomScenario randomScenario, List<ScooterDTO> scooters) {
        if (randomScenario.randomize()) {
            createRandomScooters(randomScenario.scooters());
            return;
        }

        if (scooters == null) {
            return;
        }

        for (var p : scooters) {
            var scooter = p.toScooter();
            scooter.setStatus(VehicleStatus.IDLE);
            create(scooter);
        }
    }

    public void createRandomBoats(Integer n) {
        var ports = pointOfInterestService.getAllPorts();
        for (int i = 0; i < n; i++) {
            var vehicle = new Boat("boat_" + i, VehicleType.SEA_VESSEL, true, 10);
            var boat = (Boat) create(vehicle);
            createRandomBoatInfo(boat, ports);
        }
    }

    public void createScenarioBoats(RandomScenario randomScenario, List<BoatDTO> boats) {
        if (randomScenario.randomize()) {
            createRandomBoats(randomScenario.boats());
            return;
        }

        if (boats == null) {
            return;
        }

        for (var p : boats) {
            var boat = p.toBoat();
            boat.setStatus(VehicleStatus.IDLE);
            // Return type of create is Vehicle, so we need to cast it to Boat
            boat = (Boat) create(boat);
            assignRelatedPort(pointOfInterestService.getAllPorts(), boat);
        }
    }

    private void createRandomBoatInfo(Boat boat, List<Port> ports) {
        LocationDTO newLocation = LocationDTO
                .fromGeographicPoint(ports.get(RANDOM.nextInt(ports.size())).getLocation());
        updateVehicleStatus(boat.getId(), generateRandomVehicleInfoDTO(newLocation));
    }

    private void createRandomVehicleInfo(Vehicle vehicle) {
        LocationDTO newLocation = IstanbulLocations.randomLandLocation();
        updateVehicleStatus(vehicle.getId(), generateRandomVehicleInfoDTO(newLocation));
    }

    private VehicleInfoDTO generateRandomVehicleInfoDTO(LocationDTO newLocation) {
        return new VehicleInfoDTO(
                new Battery(RANDOM.nextLong(100)),
                newLocation.latitude(),
                newLocation.longitude(),
                VehicleStatus.IDLE);
    }

}
