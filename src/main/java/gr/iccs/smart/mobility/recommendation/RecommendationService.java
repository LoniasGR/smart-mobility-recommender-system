package gr.iccs.smart.mobility.recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.neo4j.driver.internal.InternalNode;
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
import gr.iccs.smart.mobility.geojson.Feature;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.graph.GraphProjectionService;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStop;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStopService;
import gr.iccs.smart.mobility.user.User;
import gr.iccs.smart.mobility.userLandmark.UserDestinationLandmark;
import gr.iccs.smart.mobility.userLandmark.UserLandmarkService;
import gr.iccs.smart.mobility.userLandmark.UserStartLandmark;
import gr.iccs.smart.mobility.userLandmark.UserStartLandmarkDTO;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;

// TODO: Maybe create a controller for this class?
@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private BoatStopService boatStopService;

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

    public FeatureCollection createRecommendationGeoJSON(Point startingPoint, Point finishingPoint) {
        var sameSide = RecommendationUtils.areSameIstanbulSide(startingPoint, finishingPoint);

        var vehicles = recommend(startingPoint, finishingPoint);
        FeatureCollection geoJSON = new FeatureCollection();

        var startingPointFeature = GeoJSONUtils.createStartingPointFeature(startingPoint);
        var endingPointFeature = GeoJSONUtils.createDestinationPointFeature(finishingPoint);

        geoJSON.getFeatures().add(startingPointFeature);
        geoJSON.getFeatures().add(endingPointFeature);

        if (!sameSide) {
            var endingBoatStop = boatStopService.getByLocationNear(finishingPoint).getFirst();
            var endingBoatStopFeature = GeoJSONUtils.createPointFeature(endingBoatStop.getLocation(), "harbor");
            geoJSON.getFeatures().add(endingBoatStopFeature);
        }

        for (List<RecommendationDTO> rl : vehicles) {
            for (RecommendationDTO r : rl) {
                geoJSON.getFeatures().add(GeoJSONUtils.createVehicleFeature(r.vehicle()));
            }
        }
        return geoJSON;
    }

    public List<List<RecommendationDTO>> recommend(Point startingPoint, Point finishingPoint) {
        var sameSide = RecommendationUtils.areSameIstanbulSide(startingPoint, finishingPoint);

        if (sameSide) {
            // Scenario A, we only need one vehicle
            return sameSideRecommendation(startingPoint, finishingPoint);

        } else {
            // Scenario B, we need more than one vehicle
            return multiSideRecommendation(startingPoint, finishingPoint);
        }
    }

    /**
     * Handles the recommendation for moving from one side of Istanbul to the other.
     * <br>
     * TODO: Maybe split this into multiple methods?
     *
     * @param startingPoint  The location of the starting point of the user
     * @param finishingPoint The location of the end of the user's trip
     * @return A list of options. Each option is a list of recommended steps.
     */
    private List<List<RecommendationDTO>> multiSideRecommendation(Point startingPoint, Point finishingPoint) {
        var startingBoatStops = boatStopService.getByLocationNear(startingPoint);
        var endingBoatStop = boatStopService.getByLocationNear(finishingPoint).getFirst();
        var startingSeaVessels = findClosestSeaVessels(startingBoatStops);
        if (startingSeaVessels.isEmpty()) {
            return Collections.emptyList();
        }
        var seaVessel = startingSeaVessels.getFirst();
        List<List<RecommendationDTO>> suggestedVehicles = new ArrayList<>();
        suggestedVehicles.add(new ArrayList<>());

        var userSeaVesselDistance = databaseService.distance(seaVessel.getLocation(), startingPoint);
        var firstVehicle = vehicleService.findLandVesselsByLocationAround(startingPoint,
                new Distance(userSeaVesselDistance, Metrics.KILOMETERS), 1);
        if (!firstVehicle.isEmpty()
                && RecommendationUtils.areSameIstanbulSide(firstVehicle.getFirst().getLocation(), startingPoint)) {
            suggestedVehicles.getFirst().add(new RecommendationDTO(VehicleDTO.fromVehicle(firstVehicle.getFirst()),
                    LocationDTO.fromGeographicPoint(seaVessel.getLocation())));
        }
        suggestedVehicles.getFirst().add(new RecommendationDTO(VehicleDTO.fromVehicle(seaVessel),
                LocationDTO.fromGeographicPoint(endingBoatStop.getLocation())));

        var boatStopFinishingPointDistance = databaseService.distance(endingBoatStop.getLocation(), finishingPoint);
        var lastVehicle = vehicleService.findLandVesselsByLocationAround(endingBoatStop.getLocation(),
                new Distance(boatStopFinishingPointDistance, Metrics.KILOMETERS), 1);
        if (!lastVehicle.isEmpty()
                && RecommendationUtils.areSameIstanbulSide(lastVehicle.getFirst().getLocation(), finishingPoint)) {
            suggestedVehicles.getFirst().add(new RecommendationDTO(VehicleDTO.fromVehicle(lastVehicle.getFirst()),
                    LocationDTO.fromGeographicPoint(finishingPoint)));
        }
        return suggestedVehicles;
    }

    private List<List<RecommendationDTO>> sameSideRecommendation(Point startingPoint, Point finishingPoint) {
        var car = findVehicleByTypeAndLocationOnSameSide(VehicleType.CAR, startingPoint);
        var scooter = findVehicleByTypeAndLocationOnSameSide(VehicleType.SCOOTER, startingPoint);

        List<Optional<Vehicle>> maybeRecommendation = List.of(car, scooter);

        return maybeRecommendation.stream().filter(Optional::isPresent).map(Optional::get)
                .sorted(Comparator.comparing(v -> databaseService.distance(v.getLocation(), startingPoint)))
                .map(v -> List.of(new RecommendationDTO(VehicleDTO.fromVehicle(v),
                        LocationDTO.fromGeographicPoint(finishingPoint))))
                .toList();
    }

    private Optional<Vehicle> findVehicleByTypeAndLocationOnSameSide(VehicleType type, Point point) {
        var vehicle = vehicleService.findVehicleByTypeAndLocationNear(type, point, 1).getFirst();
        if (RecommendationUtils.areSameIstanbulSide(vehicle.getLocation(), point)) {
            return Optional.of(vehicle);
        }
        return Optional.empty();
    }

    private List<Vehicle> findClosestSeaVessels(List<BoatStop> boatStops) {
        for (BoatStop b : boatStops) {
            var seaVesselsInBoatStop = vehicleService.findSeaVesselsParkedInBoatStop(b.getId());
            if (!seaVesselsInBoatStop.isEmpty()) {
                return seaVesselsInBoatStop;
            }
        }
        return Collections.emptyList();
    }

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

        var nearbyBoatStops = boatStopService.getByLocationNear(startLandmark.getLocation(),
                config.getMaxWalkingDistance());
        for (var b : nearbyBoatStops) {
            var conn = connectionService.generateConnection(startLandmark, b);
            startLandmark.addConnection(conn);
        }

        neo4jTemplate.saveAs(startLandmark, UserStartLandmarkDTO.class);
    }

    private void createEndLandmarkConnections(UserDestinationLandmark destLandmark) {
        var boatStops = boatStopService.getAllWithOneLevelConnection();
        for (var b : boatStops) {
            if (databaseService.distance(b.getLocation(), destLandmark.getLocation()) <= config
                    .getMaxWalkingDistance()) {
                boatStopService.createConnectionTo(b, destLandmark);
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

    public FeatureCollection recommendationV2(Point start, Point finish, User user) {
        var startLandmark = new UserStartLandmark(start, null, user);
        var destLandmark = new UserDestinationLandmark(finish, user);
        userLandmarkService.save(destLandmark);
        userLandmarkService.save(startLandmark);

        createStartLandmarkConnections(startLandmark, destLandmark);
        createEndLandmarkConnections(destLandmark);

        final String projection = "smart-mobility";
        Map<String, Object> data = null;
        try {
            graphProjectionService.generateGraph(projection);
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
            FeatureCollection fc = new FeatureCollection();
            Point lineStart = null;
            Point lineEnd;
            for (var i : list) {
                if (i instanceof InternalNode node) {
                    Feature f = RecommendationUtils.visualiseNode(node);
                    if (fc.getFeatures().size() > 0) {
                        lineEnd = RecommendationUtils.getNodeLocation(node);
                        var line = GeoJSONUtils.createLine(lineStart, lineEnd);
                        fc.getFeatures().add(line);
                        lineStart = RecommendationUtils.getNodeLocation(node);
                    } else {
                        lineStart = RecommendationUtils.getNodeLocation(node);
                    }
                    fc.getFeatures().add(f);
                }
            }
            return fc;
        }
        return null;
    }
}
