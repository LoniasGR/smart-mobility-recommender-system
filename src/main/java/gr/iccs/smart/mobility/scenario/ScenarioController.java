package gr.iccs.smart.mobility.scenario;

import gr.iccs.smart.mobility.pointsOfInterest.PortService;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.recommendation.RecommendationDTO;
import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.usage.UseStatus;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scenario")
public class ScenarioController {
    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PortService portService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> createScenario(
            @RequestParam(defaultValue = "true") Boolean randomize,
            @RequestBody(required = false) ScenarioDTO scenario) {
        if (!vehicleService.getAll().isEmpty()) {
            throw new ScenarioException("The database is not empty, cannot create scenario.");
        }

        if (scenario == null) {
            log.info("Creating default scenario, this will fail if randomize is set to false");
        } else {
            log.info("Using scenario data");
            randomize = false;
        }
        log.debug("Creating Port Stops");
        portService.createPortScenario(randomize, scenario == null ? null : scenario.ports());

        log.debug("Creating vehicles");
        log.debug("Creating cars");
        vehicleService.createScenarioCars(randomize, scenario == null ? null : scenario.cars());
        log.debug("Creating scooters");
        vehicleService.createScenarioScooters(randomize, scenario == null ? null : scenario.scooters());
        log.debug("Creating boats");
        vehicleService.createScenarioBoats(randomize, scenario == null ? null : scenario.boats());

        log.debug("Creating users");
        userService.createScenarioUsers();

        return new ResponseEntity<>("Created scenario", HttpStatus.OK);
    }

    @PostMapping("ride/{username}")
    public void executeMovementScenario(@PathVariable String username,
            @RequestBody List<RecommendationDTO> vehicleScenarios) {
        if (userService.rideStatus(username).isPresent()) {
            throw new ScenarioException("The user is already on a ride");
        }
        var time = LocalDateTime.now();
        for (RecommendationDTO s : vehicleScenarios) {
            time = time.plusMinutes(3);
            var vehicle = vehicleService.getById(s.vehicle().id());
            UseDTO usageStart = new UseDTO(vehicle, UseStatus.ACTIVE,
                    LocationDTO.fromGeographicPoint(vehicle.getLocation()), time);
            userService.manageRide(username, usageStart);
            time = time.plusMinutes(25);
            UseDTO usageEnd = new UseDTO(vehicle, UseStatus.COMPLETED, s.destination(), time);
            userService.manageRide(username, usageEnd);
        }
    }
}
