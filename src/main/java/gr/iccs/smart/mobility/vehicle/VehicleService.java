package gr.iccs.smart.mobility.vehicle;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Iterable<Vehicle> getAll() {
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

    public void updateVehicleStatus(UUID id, VehicleRecord vehicleRecord) {
        var oldVehicle = vehicleRepository.findById(id);
        if (oldVehicle.isEmpty()) {
            throw new VehicleNotFoundException();
        }
        var vehicle = oldVehicle.get();

        vehicle.setBattery(vehicleRecord.battery());
        vehicle.setLatitude(vehicleRecord.latitude());
        vehicle.setLongitude(vehicleRecord.longitude());
        vehicle.setStatus(vehicleRecord.status());

        vehicleRepository.save(vehicle);
    }
}
