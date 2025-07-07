package gr.iccs.smart.mobility.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.TransportationPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.database.DatabaseService;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.graph.GraphProjectionService;
import gr.iccs.smart.mobility.graph.WeightType;
import gr.iccs.smart.mobility.pointsofinterest.PointOfInterestService;
import gr.iccs.smart.mobility.user.User;
import gr.iccs.smart.mobility.userlandmark.UserDestinationLandmark;
import gr.iccs.smart.mobility.userlandmark.UserLandmarkService;
import gr.iccs.smart.mobility.userlandmark.UserStartLandmark;
import gr.iccs.smart.mobility.userlandmark.UserStartLandmarkDTO;
import gr.iccs.smart.mobility.vehicle.VehicleDBService;
import gr.iccs.smart.mobility.vehicle.VehicleType;
import gr.iccs.smart.mobility.vehicle.VehicleUtilitiesService;

@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final PointOfInterestService pointOfInterestService;
    private final VehicleDBService vehicleDBService;
    private final VehicleUtilitiesService vehicleUtilitiesService;
    private final UserLandmarkService userLandmarkService;
    private final ConnectionService connectionService;
    private final TransportationPropertiesConfig config;
    private final Neo4jTemplate neo4jTemplate;
    private final DatabaseService databaseService;
    private final GraphProjectionService graphProjectionService;

    RecommendationService(PointOfInterestService pointOfInterestService, VehicleDBService vehicleDBService,
            VehicleUtilitiesService vehicleUtilitiesService, UserLandmarkService userLandmarkService,
            ConnectionService connectionService, TransportationPropertiesConfig config, Neo4jTemplate neo4jTemplate,
            DatabaseService databaseService, GraphProjectionService graphProjectionService) {
        this.pointOfInterestService = pointOfInterestService;
        this.vehicleDBService = vehicleDBService;
        this.vehicleUtilitiesService = vehicleUtilitiesService;
        this.userLandmarkService = userLandmarkService;
        this.connectionService = connectionService;
        this.config = config;
        this.neo4jTemplate = neo4jTemplate;
        this.databaseService = databaseService;
        this.graphProjectionService = graphProjectionService;
    }

    private void createStartLandmarkConnections(UserStartLandmark startLandmark, UserDestinationLandmark destLandmark) {
        log.info("Creating start landmark connections");
        var maxWalkingDistanceMeters = config.getDistances().getMaxWalkingDistanceMeters();
        if (databaseService.distance(startLandmark.getLocation(),
                destLandmark.getLocation()) < maxWalkingDistanceMeters) {
            var connection = connectionService.generateConnection(startLandmark, destLandmark);
            startLandmark.getConnections().add(connection);
        }

        var nearbyVehicles = vehicleDBService.findLandVehicleNoConnectionByNearLocation(startLandmark.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());

        for (var v : nearbyVehicles) {
            var conn = connectionService.generateConnection(startLandmark, v);
            startLandmark.addConnection(conn);
        }

        var nearbyPorts = pointOfInterestService.getPortsByLocationNear(startLandmark.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());
        for (var b : nearbyPorts) {
            var conn = connectionService.generateConnection(startLandmark, b);
            startLandmark.addConnection(conn);
        }

        var nearbyBusStops = pointOfInterestService.getBusStopsByLocationNear(startLandmark.getLocation(),
                config.getDistances().getMaxWalkingDistanceKms());
        for (var b : nearbyBusStops) {
            var conn = connectionService.generateConnection(startLandmark, b);
            startLandmark.addConnection(conn);
        }

        neo4jTemplate.saveAs(startLandmark, UserStartLandmarkDTO.class);
    }

    private void createEndLandmarkConnections(UserDestinationLandmark destLandmark) {
        var maxWalkingDistanceMeters = config.getDistances().getMaxWalkingDistanceMeters();
        var maxScooterDistanceMeters = config.getDistances().getMaxScooterDistanceMeters();
        var maxCarDistanceMeters = config.getDistances().getMaxCarDistanceMeters();
        // TODO: We should probably do it the other way around
        var ports = pointOfInterestService.getAllPortsWithOneLevelConnection();
        for (var b : ports) {
            pointOfInterestService.createConnectionFrom(b, destLandmark, maxWalkingDistanceMeters);
            pointOfInterestService.saveAndGet(b);
        }

        var landVehicles = vehicleDBService.findAllLandVehiclesWithOneLevelConnection();
        for (var v : landVehicles) {
            switch (v.getType()) {
            case VehicleType.CAR:
                vehicleDBService
                        .saveAndGet(vehicleUtilitiesService.createConnectionTo(v, destLandmark, maxCarDistanceMeters));
                break;
            case VehicleType.SCOOTER:
                vehicleDBService.saveAndGet(
                        vehicleUtilitiesService.createConnectionTo(v, destLandmark, maxScooterDistanceMeters));

                break;
            default:
                continue;
            }
        }
    }

    public List<FeatureCollection> recommendationV2(Point start, Point finish, User user,
            RecommendationOptions options) {
        var startLandmark = new UserStartLandmark(start, null, user);
        var destLandmark = new UserDestinationLandmark(finish, user);
        userLandmarkService.save(destLandmark);
        userLandmarkService.save(startLandmark);

        createStartLandmarkConnections(startLandmark, destLandmark);
        createEndLandmarkConnections(destLandmark);

        final String projection = "sm_" + user.getUsername();
        List<Map<String, Object>> data = null;

        // Generate the nodes we need to add to the graph projection
        // TODO: Make the 'BusStop' optional
        String nodes = "['UserLandmark', 'Port', 'BusStop'";
        for (var vt : VehicleType.values()) {
            if (Objects.isNull(options.requestOptions().ignoreTypes())
                    || !options.requestOptions().ignoreTypes().contains(vt)) {
                nodes += ",'" + VehicleType.nodeOf(vt) + "'";
            }
        }
        nodes += "]";

        try {
            // Ensure recommendation paths exists
            var recommendationPaths = options.requestOptions().recommendationPaths();
            if (Objects.isNull(recommendationPaths)) {
                recommendationPaths = 1;
            }

            // Ensure weight type exists
            var weightType = options.requestOptions().weightType();
            if (Objects.isNull(weightType)) {
                weightType = WeightType.distance;
            }

            graphProjectionService.generateGraph(projection, nodes);
            data = graphProjectionService.shortestPaths(projection, user.getUsername(), weightType,
                    recommendationPaths);
        } finally {
            graphProjectionService.destroyGraph(projection);
            userLandmarkService.delete(destLandmark);
            userLandmarkService.delete(startLandmark);
        }

        if (Objects.isNull(data) || data.isEmpty()) {
            throw new NoRouteFoundException("No viable route between origin and destination");
        }

        List<FeatureCollection> collections = new ArrayList<>();
        for (var d : data) {
            if (d.get("path") instanceof List<?> list) {
                var fc = RecommendationUtils.createPathFeatureCollection(list);
                if (options.wholeMap().booleanValue()) {
                    vehicleUtilitiesService.addVehiclesToGeoJSON(fc);
                }
                collections.add(fc);
            }
        }
        return collections;
    }
}
