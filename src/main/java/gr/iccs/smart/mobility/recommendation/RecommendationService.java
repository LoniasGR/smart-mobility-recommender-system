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
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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

        if(!sameSide) {
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

        if(sameSide) {
            // Scenario A, we only need one vehicle
            return sameSideRecommendation(startingPoint);

        } else {
            // Scenario B, we need more than one vehicle
            var startingBoatStops = boatStopService.getByLocationNear(startingPoint);
            var endingBoatStop = boatStopService.getByLocationNear(finishingPoint).getFirst();
            var startingSeaVessels = findClosestSeaVessels(startingBoatStops);
            if(startingSeaVessels.isEmpty()) {
                return Collections.emptyList();
            }

            var firstVehicle = vehicleService.findLandVehicleNearLocation(startingPoint, 1);
            var lastVehicle = vehicleService.findLandVehicleNearLocation(endingBoatStop.getLocation(), 1);

            return List.of(
                    firstVehicle.getFirst(),
                    startingSeaVessels.getFirst(),
                    lastVehicle.getFirst()
            );
        }
    }

    /**
     * Για απόσταση, μπορούμε να χρησιμοποιήσουμε το ίδιο με το neo4j
     * <p>
     * <a href="https://github.com/neo4j/neo4j/blob/5.17/community/values/src/main/java/org/neo4j/values/storable/CRSCalculator.java#L140">GitHub</a>
     */
    private List<Vehicle> sameSideRecommendation(Point startingPoint) {
        var car = vehicleService.findVehicleByTypeAndLocationNear(VehicleType.CAR, startingPoint, 1).getFirst();
        var scooter = vehicleService.findVehicleByTypeAndLocationNear(VehicleType.SCOOTER, startingPoint, 1).getFirst();

        var carDistance = vehicleService.distance(car.getLocation(), startingPoint);
        var scooterDistance = vehicleService.distance(scooter.getLocation(), startingPoint);

        if(carDistance > scooterDistance) {
            return List.of(scooter, car);
        }
        return List.of(car, scooter);
    }

    private List<Vehicle> findClosestSeaVessels(List<BoatStop> boatStops) {
        for(BoatStop b: boatStops) {
            var seaVesselsInBoatStop = vehicleService.findSeaVesselsParkedInBoatStop(b.getId());
            if(!seaVesselsInBoatStop.isEmpty()) {
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
