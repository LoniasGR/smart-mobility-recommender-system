package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.Random;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.pointsofinterest.PointOfInterestService;
import gr.iccs.smart.mobility.pointsofinterest.Port;
import gr.iccs.smart.mobility.scenario.RandomScenario;
import gr.iccs.smart.mobility.scenario.ScenarioDTO;

@Service
public class VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private static final Random RANDOM = new Random();

    private final VehicleRepository vehicleRepository;
    private final VehicleUtilitiesService vehicleUtilitiesService;
    private final VehicleDBService vehicleDBService;
    private final PointOfInterestService pointOfInterestService;
    private final VehicleGraphService vehicleGraphService;

    VehicleService(VehicleRepository vehicleRepository, VehicleUtilitiesService vehicleUtilitiesService,
            VehicleGraphService vehicleGraphService, VehicleDBService vehicleDBService,
            PointOfInterestService pointOfInterestService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleUtilitiesService = vehicleUtilitiesService;
        this.vehicleGraphService = vehicleGraphService;
        this.vehicleDBService = vehicleDBService;
        this.pointOfInterestService = pointOfInterestService;
    }

    public void createScenarioCars(RandomScenario randomScenario, ScenarioDTO scenario) {
        if (randomScenario.randomize().booleanValue()) {
            createRandomCars(randomScenario.cars());
            return;
        }

        List<CarDTO> cars = null;
        if (scenario == null) {
            cars = vehicleUtilitiesService.createCarsFromResourceFile().getCars();
        } else {
            cars = scenario.cars();
        }

        if (cars == null) {
            return;
        }

        for (var p : cars) {
            var car = p.toCar();
            car.setStatus(VehicleStatus.IDLE);
            vehicleDBService.createVehicle(car);
        }
    }

    public void createRandomScooters(Integer n) {
        for (int i = 0; i < n; i++) {
            var vehicle = new Scooter("scooter_" + i, VehicleType.SCOOTER, true, null);
            var scooter = vehicleDBService.createVehicle(vehicle);
            createRandomVehicleInfo(scooter);
        }
    }

    public void createScenarioScooters(RandomScenario randomScenario, List<ScooterDTO> scooters) {
        if (randomScenario.randomize().booleanValue()) {
            createRandomScooters(randomScenario.scooters());
            return;
        }

        if (scooters == null) {
            return;
        }

        for (var p : scooters) {
            var scooter = p.toScooter();
            scooter.setStatus(VehicleStatus.IDLE);
            vehicleDBService.createVehicle(scooter);
        }
    }

    public void createRandomBoats(Integer n) {
        var ports = pointOfInterestService.getAllPorts();
        for (int i = 0; i < n; i++) {
            var vehicle = new Boat("boat_" + i, VehicleType.SEA_VESSEL, true, 10);
            var boat = (Boat) vehicleDBService.createVehicle(vehicle);
            createRandomBoatInfo(boat, ports);
            addBoatToPort(vehicle.getLocation(), ports, vehicle);
        }
    }

    public void createScenarioBoats(RandomScenario randomScenario, List<BoatDTO> boats) {
        if (randomScenario.randomize().booleanValue()) {
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
            boat = (Boat) vehicleDBService.createVehicle(boat);
            assignRelatedPort(pointOfInterestService.getAllPorts(), boat);
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

    private void createRandomCars(Integer n) {
        for (int i = 0; i < n; i++) {
            var vehicle = new Car("car_" + i, VehicleType.CAR, true, null);
            var car = initialize(vehicle.toVehicleDAO());
            createRandomVehicleInfo(car);
        }
    }

    private void createRandomBoatInfo(Boat boat, List<Port> ports) {
        LocationDTO newLocation = LocationDTO
                .fromGeographicPoint(ports.get(RANDOM.nextInt(ports.size())).getLocation());
        updateVehicleInfo(boat.getId(), generateRandomVehicleInfoDTO(newLocation));
    }

    private void createRandomVehicleInfo(Vehicle vehicle) {
        LocationDTO newLocation = IstanbulLocations.randomLandLocation();
        updateVehicleInfo(vehicle.getId(), generateRandomVehicleInfoDTO(newLocation));
    }

    private VehicleInfoDTO generateRandomVehicleInfoDTO(LocationDTO newLocation) {
        return new VehicleInfoDTO(new Battery(RANDOM.nextLong(100)), newLocation, VehicleStatus.IDLE);
    }

    /**
     * Updates the location of a vehicle and optionally saves it to the database.
     */
    public Vehicle updateVehicleLocation(Vehicle vehicle, LocationDTO location, boolean shouldSave) {
        var newLocation = Values.point(4326, location.latitude(), location.longitude()).asPoint();
        vehicle.setLocation(newLocation);
        if (shouldSave) {
            return vehicleRepository.save(vehicle);
        }
        return vehicle;
    }

    public Vehicle updateVehicleLocation(String id, LocationDTO location, boolean shouldSave) {
        var vehicle = vehicleDBService.getByIdOneLevelConnections(id);
        return updateVehicleLocation(vehicle, location, shouldSave);
    }

    public Vehicle updateVehicleStatus(Vehicle vehicle, VehicleStatus status, boolean shouldSave) {
        vehicle.setStatus(status);
        if (shouldSave) {
            return vehicleRepository.save(vehicle);
        }
        return vehicle;
    }

    public Vehicle updateVehicleStatus(String id, VehicleStatus status, boolean shouldSave) {
        var vehicle = vehicleDBService.getByIdOneLevelConnections(id);
        return updateVehicleStatus(vehicle, status, shouldSave);
    }

    public Vehicle updateVehicleInfo(String id, VehicleInfoDTO vehicleInfoDTO) {
        var vehicle = vehicleDBService.getByIdOneLevelConnections(id);
        vehicle = updateVehicleLocation(vehicle, vehicleInfoDTO.location(), false);
        vehicle = updateVehicleStatus(vehicle, vehicleInfoDTO.status(), false);
        if (vehicleInfoDTO.battery() != null) {
            vehicle.setBattery(vehicleInfoDTO.battery().level());
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle initialize(VehicleDAO vehicle) {
        var v = vehicle.toVehicle();
        v.setStatus(VehicleStatus.CREATING);
        v = vehicleDBService.createVehicle(v);
        vehicleGraphService.addVehicleToGraphAsync(v);
        return v;
    }
}
