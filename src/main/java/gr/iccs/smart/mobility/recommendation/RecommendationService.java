package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.pointsOfInterest.BoatStop;
import gr.iccs.smart.mobility.pointsOfInterest.BoatStopService;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.location.InvalidLocationException;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleType;
import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.*;

// TODO: Maybe create a controller for this class?
@Service
public class RecommendationService {
    private final BoatStopService boatStopService;
    private final VehicleService vehicleService;

    public RecommendationService(BoatStopService boatStopService, VehicleService vehicleService) {
        this.boatStopService = boatStopService;
        this.vehicleService = vehicleService;
    }

    public FeatureCollection createRecommendationGeoJSON(Point startingPoint, Point finishingPoint) {
        var sameSide = areSameIstanbulSide(startingPoint, finishingPoint);

        var vehicles = recommend(startingPoint, finishingPoint);
        FeatureCollection geoJSON = new FeatureCollection();

        var startingPointFeature = GeoJSONUtils.createPointFeature(startingPoint, "arrow", "#07694c");
        var endingPointFeature = GeoJSONUtils.createPointFeature(finishingPoint, "circle-stroked", "#bd0000");

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
        var sameSide = areSameIstanbulSide(startingPoint, finishingPoint);

        if (sameSide) {
            // Scenario A, we only need one vehicle
            return sameSideRecommendation(startingPoint, finishingPoint);

        } else {
            // Scenario B, we need more than one vehicle
            return multiSideRecommendation(startingPoint, finishingPoint);
        }
    }

    /**
     * Handles the recommendation for moving from one side of Istanbul to the other. <br>
     * TODO: Maybe split this into multiple methods?
     *
     * @param startingPoint The location of the starting point of the user
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

        var userSeaVesselDistance = vehicleService.distance(seaVessel.getLocation(), startingPoint);
        var firstVehicle = vehicleService.findLandVesselsByLocationAround(startingPoint, new Distance(userSeaVesselDistance, Metrics.KILOMETERS), 1);
        if (!firstVehicle.isEmpty() && areSameIstanbulSide(firstVehicle.getFirst().getLocation(), startingPoint)) {
            suggestedVehicles.getFirst().add(new RecommendationDTO(VehicleDTO.fromVehicle(firstVehicle.getFirst()), LocationDTO.fromGeographicPoint(seaVessel.getLocation())));
        }
        suggestedVehicles.getFirst().add(new RecommendationDTO(VehicleDTO.fromVehicle(seaVessel), LocationDTO.fromGeographicPoint(endingBoatStop.getLocation())));

        var boatStopFinishingPointDistance = vehicleService.distance(endingBoatStop.getLocation(), finishingPoint);
        var lastVehicle = vehicleService.findLandVesselsByLocationAround(endingBoatStop.getLocation(), new Distance(boatStopFinishingPointDistance, Metrics.KILOMETERS), 1);
        if (!lastVehicle.isEmpty() && areSameIstanbulSide(lastVehicle.getFirst().getLocation(), finishingPoint)) {
            suggestedVehicles.getFirst().add(new RecommendationDTO(VehicleDTO.fromVehicle(lastVehicle.getFirst()), LocationDTO.fromGeographicPoint(finishingPoint)));
        }
        return suggestedVehicles;
    }

    private List<List<RecommendationDTO>> sameSideRecommendation(Point startingPoint, Point finishingPoint) {
        var car = findVehicleByTypeAndLocationOnSameSide(VehicleType.CAR, startingPoint);
        var scooter = findVehicleByTypeAndLocationOnSameSide(VehicleType.SCOOTER, startingPoint);

        List<Optional<Vehicle>> maybeRecommendation = List.of(car, scooter);

        return maybeRecommendation.stream().filter(Optional::isPresent).map(Optional::get).sorted(Comparator.comparing(v -> vehicleService.distance(v.getLocation(), startingPoint))).map(v -> List.of(new RecommendationDTO(VehicleDTO.fromVehicle(v), LocationDTO.fromGeographicPoint(finishingPoint)))).toList();
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

        if (startingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA || finishingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA) {
            throw new InvalidLocationException("The starting and ending locations must not be on the sea");
        }
        return startingPointPosition == finishingPointPosition;
    }
}
