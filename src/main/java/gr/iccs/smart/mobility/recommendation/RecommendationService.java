package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.boatStop.BoatStop;
import gr.iccs.smart.mobility.boatStop.BoatStopService;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONService;
import gr.iccs.smart.mobility.location.InvalidLocationException;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;
import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {
    private final BoatStopService boatStopService;
    private final VehicleService vehicleService;
    private final GeoJSONService geoJSONService;

    public RecommendationService(BoatStopService boatStopService,
                                 VehicleService vehicleService,
                                 GeoJSONService geoJSONService) {
        this.boatStopService = boatStopService;
        this.vehicleService = vehicleService;
        this.geoJSONService = geoJSONService;
    }

    public FeatureCollection createRecommendationGeoJSON(Point startingPoint, Point finishingPoint) {
        var sameSide = areSameIstanbulSide(startingPoint, finishingPoint);

        var vehicles = recommend(startingPoint, finishingPoint);
        FeatureCollection geoJSON = new FeatureCollection();

        var startingPointFeature = geoJSONService.createPointFeature(startingPoint, "arrow", "#07694c");
        var endingPointFeature = geoJSONService.createPointFeature(finishingPoint, "circle-stroked", "#bd0000");

        geoJSON.getFeatures().add(startingPointFeature);
        geoJSON.getFeatures().add(endingPointFeature);

        if (!sameSide) {
            var endingBoatStop = boatStopService.getByLocationNear(finishingPoint).getFirst();
            var endingBoatStopFeature = geoJSONService.createPointFeature(endingBoatStop.getLocation(), "harbor");
            geoJSON.getFeatures().add(endingBoatStopFeature);
        }

        for (Vehicle v : vehicles) {
            geoJSON.getFeatures().add(geoJSONService.createVehicleFeature(v));
        }
        return geoJSON;
    }

    public List<Vehicle> recommend(Point startingPoint, Point finishingPoint) {
        var sameSide = areSameIstanbulSide(startingPoint, finishingPoint);

        if (sameSide) {
            // Scenario A, we only need one vehicle
            return sameSideRecommendation(startingPoint);

        } else {
            // Scenario B, we need more than one vehicle
            return multiSideRecommendation(startingPoint, finishingPoint);
        }
    }

    private List<Vehicle> multiSideRecommendation(Point startingPoint, Point finishingPoint) {
        var startingBoatStops = boatStopService.getByLocationNear(startingPoint);
        var endingBoatStop = boatStopService.getByLocationNear(finishingPoint).getFirst();
        var startingSeaVessels = findClosestSeaVessels(startingBoatStops);
        if (startingSeaVessels.isEmpty()) {
            return Collections.emptyList();
        }
        var seaVessel = startingSeaVessels.getFirst();
        List<Vehicle> suggestedVehicles = new ArrayList<>();

        var userSeaVesselDistance = vehicleService.distance(seaVessel.getLocation(), startingPoint);
        var firstVehicle = vehicleService.findLandVesselsByLocationAround(
                startingPoint,
                new Distance(userSeaVesselDistance, Metrics.KILOMETERS),
                1);
        if (!firstVehicle.isEmpty() && areSameIstanbulSide(firstVehicle.getFirst().getLocation(), startingPoint)) {
            suggestedVehicles.add(firstVehicle.getFirst());
        }
        suggestedVehicles.add(seaVessel);

        var boatStopFinishingPointDistance = vehicleService.distance(endingBoatStop.getLocation(), finishingPoint);
        var lastVehicle = vehicleService.findLandVesselsByLocationAround(
                endingBoatStop.getLocation(),
                new Distance(boatStopFinishingPointDistance, Metrics.KILOMETERS),
                1);
        if (!lastVehicle.isEmpty() && areSameIstanbulSide(lastVehicle.getFirst().getLocation(), finishingPoint)) {
            suggestedVehicles.add(lastVehicle.getFirst());
        }
        return suggestedVehicles;
    }


    /**
     * Για απόσταση, μπορούμε να χρησιμοποιήσουμε το ίδιο με το neo4j
     * <p>
     * <a href="https://github.com/neo4j/neo4j/blob/5.17/community/values/src/main/java/org/neo4j/values/storable/CRSCalculator.java#L140">GitHub</a>
     */
    private List<Vehicle> sameSideRecommendation(Point startingPoint) {
        var car = findVehicleByTypeAndLocationOnSameSide(VehicleType.CAR, startingPoint);
        var scooter = findVehicleByTypeAndLocationOnSameSide(VehicleType.SCOOTER, startingPoint);

        List<Optional<Vehicle>> maybeRecommendation = List.of(car, scooter);

        return maybeRecommendation.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(v -> vehicleService.distance(v.getLocation(), startingPoint)))
                .toList();
    }

    private Optional<Vehicle> findVehicleByTypeAndLocationOnSameSide(VehicleType type, Point point) {
        var vehicle = vehicleService.findVehicleByTypeAndLocationNear(type, point, 1).getFirst();
        if (areSameIstanbulSide(vehicle.getLocation(), point)) {
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

    private Boolean areSameIstanbulSide(Point startingPoint, Point finishingPoint) {
        var startingPointPosition = LocationDTO.istanbulLocation(startingPoint);
        var finishingPointPosition = LocationDTO.istanbulLocation(finishingPoint);

        if (startingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA ||
                finishingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA) {
            throw new InvalidLocationException("The starting and ending locations must not be on the sea");
        }
        return startingPointPosition == finishingPointPosition;
    }
}
