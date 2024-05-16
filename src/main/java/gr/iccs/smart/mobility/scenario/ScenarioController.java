package gr.iccs.smart.mobility.scenario;

import gr.iccs.smart.mobility.boatStop.BoatStopService;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scenario")
public class ScenarioController {
    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);
    private final VehicleService vehicleService;
    private final BoatStopService boatStopService;
    private final UserService userService;

    public ScenarioController(VehicleService vehicleService, BoatStopService boatStopService, UserService userService) {
        this.vehicleService = vehicleService;
        this.boatStopService = boatStopService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> createScenario() {
        if (!vehicleService.getAll().isEmpty()) {
            return new ResponseEntity<>("The database is not empty, cannot create scenario.", HttpStatus.BAD_REQUEST);
        }
        log.debug("Creating Boat Stops");
        boatStopService.createBoatStopScenario();

        log.debug("Creating vehicles");
        vehicleService.createScenarioVehicles();

        log.debug("Creating users");
        userService.createScenarioUsers();

        log.debug("Positioning vehicles");
        vehicleService.createScenarioLocations();

        return new ResponseEntity<>("Created scenario", HttpStatus.OK);
    }
}
