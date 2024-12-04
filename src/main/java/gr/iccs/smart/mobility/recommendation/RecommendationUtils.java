package gr.iccs.smart.mobility.recommendation;

import java.util.List;
import java.util.UUID;

import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.geojson.Feature;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.location.InvalidLocationException;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;
import gr.iccs.smart.mobility.vehicle.VehicleStatus;
import gr.iccs.smart.mobility.vehicle.VehicleType;

public class RecommendationUtils {

    protected static Boolean areSameIstanbulSide(Point startingPoint, Point finishingPoint) {
        var startingPointPosition = LocationDTO.istanbulLocation(startingPoint);
        var finishingPointPosition = LocationDTO.istanbulLocation(finishingPoint);

        if (startingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA
                || finishingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA) {
            throw new InvalidLocationException("The starting and ending locations must not be on the sea");
        }
        return startingPointPosition == finishingPointPosition;
    }

    protected static Feature visualiseNode(InternalNode node) {
        var location = getNodeLocation(node);
        Feature f = null;
        if (node.labels().contains("UserStartLandmark")) {
            f = GeoJSONUtils.createStartingPointFeature(location);
        }
        if (node.labels().contains("UserDestinationLandmark")) {
            f = GeoJSONUtils.createDestinationPointFeature(location);
        }
        if (node.labels().contains("LandVehicle")) {
            var vehicle = new VehicleDTO(
                    UUID.fromString(node.get("id").asString()),
                    Enum.valueOf(VehicleType.class, node.get("type").asString()),
                    node.get("battery").asDouble(),
                    location,
                    Enum.valueOf(VehicleStatus.class, node.get("status").asString()));
            f = GeoJSONUtils.createVehicleFeature(vehicle);
        }
        if (node.labels().contains("BoatStop")) {
            f = GeoJSONUtils.createBoatStopFeature(location);
        }

        return f;
    }

    protected static Point getNodeLocation(InternalNode node) {
        return node.get("location").asPoint();
    }

    protected static FeatureCollection createPathFeatureCollection(List<?> points) {
        FeatureCollection fc = new FeatureCollection();
        Point lineStart = null;
        Point lineEnd;
        for (var i : points) {
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
}
