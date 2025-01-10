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

    public void createScenario(ScenarioDTO scenario, boolean randomize) {

        if (!vehicleService.getAll().isEmpty()) {
            throw new ScenarioException("The database is not empty, cannot create scenario.");
        }

        if (scenario == null) {
            log.info("Creating default scenario, this will fail if randomize is set to false");
        } else {
            log.info("Using scenario data");
            randomize = false;
        }
        log.debug("Creating Ports");
        portService.createPortScenario(randomize, scenario);

        log.debug("Creating Vehicles");

        log.debug("Creating Cars");
        vehicleService.createScenarioCars(randomize, scenario);

        log.debug("Creating Scooters");
        vehicleService.createScenarioScooters(randomize, scenario == null ? null : scenario.scooters());

        log.debug("Creating Boats");
        vehicleService.createScenarioBoats(randomize, scenario == null ? null : scenario.boats());

        log.debug("Creating users");
        userService.createScenarioUsers();

        log.debug("Creating graph");
        graphService.graphPreCalculation();
    }

}
