package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.usage.Used;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VehicleService vehicleService;

    public UserService(UserRepository userRepository, VehicleService vehicleService) {
        this.userRepository = userRepository;
        this.vehicleService = vehicleService;
    }

    public Iterable<User> getAll() {
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
        if(userRepository.existsById(user.getUsername())) {
            throw new PersonBadRequest("Person already exists");
        }
        return userRepository.save(user);
    }

    public void addRide(String username, Used useInfo) {
        var person = getById(username);
        var vehicle = vehicleService.getById(useInfo.getVehicle().getId());
        useInfo.setVehicle(vehicle);


        person.getVehicles().add(useInfo);
        userRepository.save(person);
    }

}
