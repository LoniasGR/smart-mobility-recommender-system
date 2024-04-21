package gr.iccs.smart.mobility.vehicle;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle create(Vehicle vehicle) {
        if (vehicle.getType() == null) {
            throw new BadVehicleRequest("vehicle type is missing");
        }

        if(vehicleRepository.existsById(vehicle.getId())) {
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

        vehicle.setBattery(vehicleInfoDTO.battery());
        vehicle.setLocation(Values.point(4326, vehicleInfoDTO.latitude(), vehicleInfoDTO.longitude()).asPoint());
        vehicle.setStatus(vehicleInfoDTO.status());

        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> findNearLocation(Point point, Distance maxDistance) {
        if(maxDistance == null) {
            return vehicleRepository.findByLocationNear(point);
        }
        return vehicleRepository.findByLocationNear(point, maxDistance);
    }
}
