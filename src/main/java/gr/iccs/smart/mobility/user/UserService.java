package gr.iccs.smart.mobility.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.usage.RideStatusUpdateEventPublisher;
import gr.iccs.smart.mobility.usage.UsageService;
import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.usage.UseStatus;
import gr.iccs.smart.mobility.usage.Used;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDBService;
import net.datafaker.Faker;

@Service
public class UserService {

    private UserRepository userRepository;
    private VehicleDBService vehicleDBService;
    private UsageService usageService;
    private RideStatusUpdateEventPublisher rideStatusUpdateEventPublisher;

    UserService(UserRepository userRepository, VehicleDBService vehicleDBService, UsageService usageService,
            RideStatusUpdateEventPublisher rideStatusUpdateEventPublisher) {
        this.userRepository = userRepository;
        this.vehicleDBService = vehicleDBService;
        this.usageService = usageService;
        this.rideStatusUpdateEventPublisher = rideStatusUpdateEventPublisher;
    }

    private final Faker faker = new Faker();

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(String username) {
        var person = userRepository.findByUsername(username);
        if (person.isPresent()) {
            return person.get();
        }
        throw new PersonNotFoundException();
    }

    public User update(User user) {
        if (!userRepository.existsById(user.getUsername())) {
            throw new PersonNotFoundException();
        }

        return userRepository.save(user);
    }

    public void deleteReservation(User user, Vehicle vehicle) {
        userRepository.deleteReservation(user.getUsername(), vehicle.getId());
    }

    public User create(User user) {
        if (userRepository.existsById(user.getUsername())) {
            throw new PersonBadRequest("Person already exists");
        }
        return userRepository.save(user);
    }

    /***
     * Handles the creation and update of the ride state.
     * 
     * @param username
     * @param useInfo
     */
    public void manageRide(String username, UseDTO useInfo) {
        var vehicle = vehicleDBService.getLandVehicleByIdNoConnections(useInfo.vehicle().getId());

        var person = getById(username);
        var ride = person.getCurrentRide();

        // The user does not have an active ride, so this call should be creating one
        if (ride.isEmpty()) {
            if (useInfo.status().equals(UseStatus.COMPLETED)) {
                throw new IllegalArgumentException("There is no ride to be completed.");
            }
            var newRide = usageService.createOrUpdateRide(null, useInfo, vehicle);
            person.getVehiclesUsed().add(newRide);
            // The user already had an active ride, so this should be terminating it.
        } else {
            if (useInfo.status().equals(UseStatus.ACTIVE)) {
                throw new IllegalArgumentException("A new ride cannot be started while one is already in use.");
            }
            usageService.createOrUpdateRide(ride.get(), useInfo, null);
        }
        userRepository.save(person);
        rideStatusUpdateEventPublisher.publishRideStatusUpdateEvent(useInfo, vehicle.getId());
    }

    public Optional<Used> rideStatus(String username) {
        var person = getById(username);
        return person.getCurrentRide();
    }

    public void createScenarioUsers() {
        var customUser = new User("test.user", null, null);
        create(customUser);
        for (int i = 0; i < 10; i++) {
            var user = new User(faker.internet().username(), null, null);
            create(user);
        }
    }
}
