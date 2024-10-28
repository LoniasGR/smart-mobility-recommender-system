package gr.iccs.smart.mobility.user;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.usage.UseStatus;
import gr.iccs.smart.mobility.usage.Used;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleInfoDTO;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleStatus;
import net.datafaker.Faker;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleService vehicleService;

    private final Faker faker = new Faker();

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(String username) {
        var person = userRepository.findById(username);
        if (person.isPresent()) {
            return person.get();
        }
        throw new PersonNotFoundException();
    }

    public User create(User user) {
        if (userRepository.existsById(user.getUsername())) {
            throw new PersonBadRequest("Person already exists");
        }
        return userRepository.save(user);
    }

    public void manageRide(String username, UseDTO useInfo) {
        Vehicle vehicle = vehicleService.getById(useInfo.vehicle().getId());
        VehicleStatus vehicleStatus;

        var person = getById(username);
        var ride = person.getCurrentRide();

        // The user does not have an active ride, so this call should be creating one
        if (ride.isEmpty()) {
            if (useInfo.status().equals(UseStatus.COMPLETED)) {
                throw new IllegalArgumentException("There is no ride to be completed.");
            }
            var newRide = createOrUpdateRide(null, useInfo, vehicle);
            person.getVehiclesUsed().add(newRide);
            vehicleStatus = VehicleStatus.IN_USE;

        } else {
            if (useInfo.status().equals(UseStatus.ACTIVE)) {
                throw new IllegalArgumentException("A new ride cannot be started while one is already in use.");
            }
            createOrUpdateRide(ride.get(), useInfo, null);
            vehicleStatus = VehicleStatus.IDLE;
        }
        var vehicleInfo = new VehicleInfoDTO(null,
                useInfo.location().latitude(),
                useInfo.location().longitude(),
                vehicleStatus);
        userRepository.save(person);
        vehicleService.updateVehicleStatus(vehicle.getId(), vehicleInfo);
    }

    private Used createOrUpdateRide(Used rideInfo, UseDTO useInfo, Vehicle vehicle) {
        var location = useInfo.location().toPoint();
        var ride = rideInfo;
        if (ride == null) {
            ride = new Used();
            ride.setVehicle(vehicle);
            ride.setStartingLocation(location);
            ride.setStartingTime(useInfo.time());
        } else {
            ride.setEndingLocation(location);
            ride.setEndingTime(useInfo.time());
        }
        ride.setStatus(useInfo.status());
        return ride;
    }

    public Optional<Used> rideStatus(String username) {
        var person = getById(username);
        return person.getCurrentRide();
    }

    public void createScenarioUsers() {
        var custom_user = new User("test.user", null);
        create(custom_user);
        for (int i = 0; i < 10; i++) {
            var user = new User(faker.internet().username(), null);
            create(user);
        }
    }
}
