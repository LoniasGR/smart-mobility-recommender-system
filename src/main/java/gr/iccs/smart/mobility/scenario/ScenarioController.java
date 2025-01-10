package gr.iccs.smart.mobility.scenario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.recommendation.RecommendationDTO;
import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.usage.UseStatus;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.VehicleService;

@RestController
@RequestMapping("/api/scenario")
public class ScenarioController {
    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> createScenario(
            @RequestParam(defaultValue = "true") Boolean randomize,
            @RequestBody(required = false) ScenarioDTO scenario) {

        log.debug("Called createScenario with randomize: " + randomize.toString() + " and " +
                (Objects.isNull(scenario) ? "provided" : "no") + " scenario");

        scenarioService.createScenario(scenario, randomize);
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
