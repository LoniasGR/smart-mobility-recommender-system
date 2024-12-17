package gr.iccs.smart.mobility.recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.config.MovementPropertiesConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.database.DatabaseService;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.graph.GraphProjectionService;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.pointsOfInterest.Port;
import gr.iccs.smart.mobility.pointsOfInterest.PortService;
import gr.iccs.smart.mobility.user.User;
import gr.iccs.smart.mobility.userLandmark.UserDestinationLandmark;
import gr.iccs.smart.mobility.userLandmark.UserLandmarkService;
import gr.iccs.smart.mobility.userLandmark.UserStartLandmark;
import gr.iccs.smart.mobility.userLandmark.UserStartLandmarkDTO;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;

@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private PortService portService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserLandmarkService userLandmarkService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private MovementPropertiesConfig config;

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private GraphProjectionService graphProjectionService;

    private void createStartLandmarkConnections(UserStartLandmark startLandmark, UserDestinationLandmark destLandmark) {
        if (databaseService.distance(startLandmark.getLocation(), destLandmark.getLocation()) < config
                .getMaxWalkingDistance()) {
            var connection = connectionService.generateConnection(startLandmark, destLandmark);
            startLandmark.getConnections().add(connection);
        }

        var nearbyVehicles = vehicleService.findLandVehicleNoConnectionByNearLocation(startLandmark.getLocation(),
                config.getMaxWalkingDistance());

        for (var v : nearbyVehicles) {
            var conn = connectionService.generateConnection(startLandmark, v);
            startLandmark.addConnection(conn);
        }

        var nearbyPorts = portService.getByLocationNear(startLandmark.getLocation(),
                config.getMaxWalkingDistance());
        for (var b : nearbyPorts) {
            var conn = connectionService.generateConnection(startLandmark, b);
            startLandmark.addConnection(conn);
        }

        neo4jTemplate.saveAs(startLandmark, UserStartLandmarkDTO.class);
    }

    private void createEndLandmarkConnections(UserDestinationLandmark destLandmark) {
        var ports = portService.getAllWithOneLevelConnection();
        for (var b : ports) {
            if (databaseService.distance(b.getLocation(), destLandmark.getLocation()) <= config
                    .getMaxWalkingDistance()) {
                portService.createConnectionTo(b, destLandmark);
            }
        }

        var landVehicles = vehicleService.getAllLandVehiclesWithOneLevelConnection();
        for (var v : landVehicles) {
            switch (v.getType()) {
                case VehicleType.CAR:
                    vehicleService.createConnectionTo(v, destLandmark);
                    break;
                case VehicleType.SCOOTER:
                    if (databaseService.distance(v.getLocation(),
                            destLandmark.getLocation()) <= config.getMaxScooterDistance()) {
                        vehicleService.createConnectionTo(v, destLandmark);
                    }
                    break;
                default:
                    continue;
            }
        }
    }

    public FeatureCollection recommendationV2(Point start, Point finish, User user, RecommendationOptions options) {
        var startLandmark = new UserStartLandmark(start, null, user);
        var destLandmark = new UserDestinationLandmark(finish, user);
        userLandmarkService.save(destLandmark);
        userLandmarkService.save(startLandmark);

        createStartLandmarkConnections(startLandmark, destLandmark);
        createEndLandmarkConnections(destLandmark);

        final String projection = "smart-mobility";
        Map<String, Object> data = null;
        try {
            String nodes = "['LandVehicle', 'UserLandmark', 'Port']";
            // for (var vt : VehicleType.values()) {
            // if (!options.requestOptions().ignoreTypes().contains(vt)) {
            // nodes += ",'" + vt.toString() + "'";
            // }
            // }
            // nodes += "]";
            graphProjectionService.generateGraph(projection, nodes);
            data = graphProjectionService.shortestPaths(projection, user.getUsername());
        } finally {
            graphProjectionService.destroyGraph(projection);
            userLandmarkService.delete(destLandmark);
            userLandmarkService.delete(startLandmark);
        }

        if (data.isEmpty() || Objects.isNull(data)) {
            // There should be an error here.
            return null;
        }
        if (data.get("path") instanceof List<?> list) {
            var fc = RecommendationUtils.createPathFeatureCollection(list);
            if (options.wholeMap()) {
                vehicleService.addVehiclesToGeoJSON(fc);
            }
            return fc;
        }

        // This should be an error or unreachable
        return null;
    }
}
