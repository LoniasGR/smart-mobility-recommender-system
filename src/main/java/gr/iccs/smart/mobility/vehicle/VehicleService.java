package gr.iccs.smart.mobility.vehicle;

import gr.iccs.smart.mobility.boatStop.BoatStop;
import gr.iccs.smart.mobility.boatStop.BoatStopService;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private static final Random RANDOM = new Random();
    private final VehicleRepository vehicleRepository;
    private final BoatStopService boatStopService;

    public VehicleService(VehicleRepository vehicleRepository, BoatStopService boatStopService) {
        this.vehicleRepository = vehicleRepository;
        this.boatStopService = boatStopService;
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


        if(vehicle.getType() == VehicleType.SEA_VESSEL) {
          updateRelatedBoatStops(newLocation, boatStops, vehicle);
        }
        vehicle.setLocation(newLocation);
        vehicle.setStatus(vehicleInfoDTO.status());
        vehicle.setBattery(vehicleInfoDTO.battery());
        return vehicleRepository.save(vehicle);
    }

    private void validateLocation(Point newLocation, List<BoatStop> boatStops, VehicleType vehicleType) {
        var isSeaLocation = LocationDTO.istanbulLocation(newLocation) == IstanbulLocations.IstanbulLocationDescription.SEA;
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

        // If the sea vessel is parked in a boat stop, add it to the list of boats in the stop
        if(coastLocation.isPresent()) {
            coastLocation.get().getParkedVehicles().add(vehicle);
            boatStopService.update(coastLocation.get());
        }
        // Otherwise remove it if it's leaving a boat stop
        else {
            var oldBoatStop = boatStopService.getByExactLocation(vehicle.getLocation());
            if(oldBoatStop.isPresent()) {
                oldBoatStop.get().getParkedVehicles().remove(vehicle);
                boatStopService.update(oldBoatStop.get());
            }
        }
    }

    public List<Vehicle> findNearLocation(Point point, Distance maxDistance) {
        if (maxDistance == null) {
            return vehicleRepository.findByLocationNear(point);
        }
        return vehicleRepository.findByLocationNear(point, maxDistance);
    }

    public void createScenarioVehicles() {
        for (int i = 0; i < 100; i++) {
            var vehicle = new Vehicle(UUID.randomUUID(), VehicleType.randomVehicleType());
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
}
