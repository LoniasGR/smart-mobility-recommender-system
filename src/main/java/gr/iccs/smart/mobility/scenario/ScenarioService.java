package gr.iccs.smart.mobility.scenario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.graph.GraphService;
import gr.iccs.smart.mobility.pointsOfInterest.PortService;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.VehicleService;

@Service
public class ScenarioService {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PortService portService;

    @Autowired
    private UserService userService;

    @Autowired
    private GraphService graphService;

    private static final Logger log = LoggerFactory.getLogger(ScenarioService.class);

    public void createScenario(ScenarioDTO scenario, RandomScenario randomScenario) {

        if (!vehicleService.getAll().isEmpty()) {
            throw new ScenarioException("The database is not empty, cannot create scenario.");
        }

        if (scenario != null && randomScenario.randomize()) {
            throw new ScenarioException("Cannot combine both a predifined and a random scenario");
        }

        if (scenario == null) {
            log.info("Creating default scenario");
        } else {
            log.info("Using scenario data");
        }

        log.debug("Creating Ports");
        portService.createPortScenario(randomScenario, scenario);

        log.debug("Creating Vehicles");

        log.debug("Creating Cars");
        vehicleService.createScenarioCars(randomScenario, scenario);

        log.debug("Creating Scooters");
        vehicleService.createScenarioScooters(randomScenario, scenario == null ? null : scenario.scooters());

        log.debug("Creating Boats");
        vehicleService.createScenarioBoats(randomScenario, scenario == null ? null : scenario.boats());

        log.debug("Creating users");
        userService.createScenarioUsers();

        log.debug("Creating graph");
        graphService.graphPreCalculation();
    }

}
