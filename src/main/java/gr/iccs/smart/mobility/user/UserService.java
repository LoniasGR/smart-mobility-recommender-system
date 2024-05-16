package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.usage.UseStatus;
import gr.iccs.smart.mobility.usage.Used;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VehicleService vehicleService;

    private final Faker faker = new Faker();

    public UserService(UserRepository userRepository, VehicleService vehicleService) {
        this.userRepository = userRepository;
        this.vehicleService = vehicleService;
    }

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
        var person = getById(username);
        var vehicle = vehicleService.getById(useInfo.vehicle().getId());
        var ride = person.getCurrentRide();

        // The user does not have an active ride, so this call should be creating one
        if (ride.isEmpty()) {
            if (useInfo.status().equals(UseStatus.COMPLETED)) {
                throw new IllegalArgumentException("There is no ride to be completed.");
            }
            var newRide = new Used();
            newRide.setVehicle(vehicle);
            newRide.setStatus(useInfo.status());
            var startLocation = useInfo.location().toPoint();
            newRide.setStartingLocation(startLocation);
            person.getVehiclesUsed().add(newRide);
        } else {
            if (useInfo.status().equals(UseStatus.ACTIVE)) {
                throw new IllegalArgumentException("A new ride cannot be started while one is already in use");
            }
            var currentRide = ride.get();
            currentRide.setStatus(useInfo.status());
            var endingLocation = useInfo.location().toPoint();
            currentRide.setEndingLocation(endingLocation);
            currentRide.setEndingTime(useInfo.time());
        }
        userRepository.save(person);
    }

    public Optional<Used> rideStatus(String username) {
        var person = getById(username);
        return person.getCurrentRide();
    }

    public void createScenarioUsers() {
        for (int i = 0; i < 10; i++) {
            var user = new User(faker.internet().username(), null);
            create(user);
        }
    }
}
