package gr.iccs.smart.mobility.scenario;

import gr.iccs.smart.mobility.pointsOfInterest.BoatStopService;
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
    private BoatStopService boatStopService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> createScenario() {
        if (!vehicleService.getAll().isEmpty()) {
            throw new ScenarioException("The database is not empty, cannot create scenario.");
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
