package gr.iccs.smart.mobility.recommendation;

import java.util.List;

import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.geojson.Feature;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.pointsofinterest.BusStopDTO;
import gr.iccs.smart.mobility.pointsofinterest.PortDTO;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;
import gr.iccs.smart.mobility.vehicle.VehicleStatus;
import gr.iccs.smart.mobility.vehicle.VehicleType;

public class RecommendationUtils {
    protected static Feature visualiseNode(InternalNode node) {
        var location = getNodeLocation(node);
        // There are multiple labels for each node, but only one is relevant
        for (String label : node.labels()) {
            switch (label) {
            case "UserStartLandmark":
                return GeoJSONUtils.createStartingPointFeature(location);
            case "UserDestinationLandmark":
                return GeoJSONUtils.createDestinationPointFeature(location);
            case "LandVehicle":
                var vehicle = new VehicleDTO(node.get("id").asString(),
                        Enum.valueOf(VehicleType.class, node.get("type").asString()), node.get("battery").asLong(),
                        node.get("dummy").asBoolean(), location,
                        Enum.valueOf(VehicleStatus.class, node.get("status").asString()));
                return GeoJSONUtils.createVehicleFeature(vehicle);
            case "Port":
                var port = new PortDTO(node.get("id").asString(), node.get("name").asString(),
                        LocationDTO.fromGeographicPoint(location));
                return GeoJSONUtils.createPortFeature(port);
            case "BusStop":
                var busStop = new BusStopDTO(node.get("id").asString(), node.get("name").asString(),
                        LocationDTO.fromGeographicPoint(location));
                return GeoJSONUtils.createBusStopFeature(busStop);
            case "ReachableNode":
            case "UserLandmark":
                continue;
            }
        }
        throw new IllegalArgumentException("Node with labels: " + node.labels() + " does not have a valid label");
    }

    protected static Point getNodeLocation(InternalNode node) {
        return node.get("location").asPoint();
    }

    private static String extractSegmentMode(InternalNode node) {
        for (String label : node.labels()) {
            switch (label) {
            case "UserStartLandmark":
                return "walk";
            case "Car":
                return "passenger_car";
            case "Scooter":
                return "e_scooter";
            case "Port":
                return "sea_vessel";
            case "BusStop":
                return "public_transport";
            case "ReachableNode":
            case "UserLandmark":
            case "LandVehicle":
            case "UserDestinationLandmark":
                continue;
            }
        }
        return "";
    }

    protected static FeatureCollection createPathFeatureCollection(List<?> points) {
        FeatureCollection fc = new FeatureCollection();
        Point lineStart = null;
        Point lineEnd;
        String segmentMode = "walk";

        for (var i : points) {
            if (i instanceof InternalNode node) {
                Feature f = RecommendationUtils.visualiseNode(node);
                if (fc.getFeatures().size() > 0) {
                    lineEnd = RecommendationUtils.getNodeLocation(node);
                    var line = GeoJSONUtils.createLine(lineStart, lineEnd);
                    line.getProperties().put("segment_mode", segmentMode);
                    fc.getFeatures().add(line);
                    lineStart = RecommendationUtils.getNodeLocation(node);
                    segmentMode = RecommendationUtils.extractSegmentMode(node);
                } else {
                    lineStart = RecommendationUtils.getNodeLocation(node);
                    segmentMode = RecommendationUtils.extractSegmentMode(node);

                }
                fc.getFeatures().add(f);
            }
        }
        return fc;
    }
}
