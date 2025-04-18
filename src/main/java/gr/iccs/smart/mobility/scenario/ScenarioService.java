package gr.iccs.smart.mobility.scenario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.database.DatabaseService;
import gr.iccs.smart.mobility.graph.GraphService;
import gr.iccs.smart.mobility.pointsOfInterest.PointOfInterestService;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.VehicleService;

@Service
public class ScenarioService {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PointOfInterestService pointOfInterestService;

    @Autowired
    private UserService userService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private DatabaseService databaseService;

    private static final Logger log = LoggerFactory.getLogger(ScenarioService.class);

    public void createScenario(ScenarioDTO scenario, RandomScenario randomScenario, boolean force) {

        if (!vehicleService.getAll().isEmpty() || !pointOfInterestService.getAll().isEmpty()) {
            if (!force) {
                throw new ScenarioException("The database is not empty, cannot create scenario.");
            }
            databaseService.clearDatabase();
        }

        if (scenario != null && randomScenario.randomize()) {
            throw new ScenarioException("Cannot combine both a predifined and a random scenario");
        }

        if (scenario == null) {
            log.info("Creating default scenario");
        } else {
            log.info("Using scenario data");
        }

        log.info("Creating Ports");
        pointOfInterestService.createPortScenario(randomScenario, scenario);

        log.info("Creating bus stops");
        pointOfInterestService.createBusStopScenario(randomScenario, scenario == null ? null : scenario.busStops());

        log.info("Creating Vehicles");

        log.info("Creating Cars");
        vehicleService.createScenarioCars(randomScenario, scenario);

        log.info("Creating Scooters");
        vehicleService.createScenarioScooters(randomScenario, scenario == null ? null : scenario.scooters());

        log.info("Creating Boats");
        vehicleService.createScenarioBoats(randomScenario, scenario == null ? null : scenario.boats());

        log.info("Creating users");
        userService.createScenarioUsers();

        log.info("Creating graph");
        graphService.graphPreCalculation();
    }

}
