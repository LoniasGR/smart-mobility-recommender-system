package gr.iccs.smart.mobility.geojson;

import org.neo4j.driver.types.Point;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;

@Service
public class GeoJSONUtils {

    public static Feature createVehicleFeature(Vehicle v) {
        return createVehicleFeature(VehicleDTO.fromVehicle(v));
    }

    public static Feature createVehicleFeature(VehicleDTO v) {
        Feature f = new Feature();
        f.setGeometry(new gr.iccs.smart.mobility.geojson.Point(v.location().y(), v.location().x()));

        f.getProperties().put("type", v.type().toString());
        f.getProperties().put("id", v.id().toString());
        switch (v.type()) {
            case SEA_VESSEL:
                f.getProperties().put("marker-symbol", "ferry");
                f.getProperties().put("marker-color", "#5fd60f");
                break;
            case CAR:
                f.getProperties().put("marker-symbol", "car");
                f.getProperties().put("marker-color", "#b11313");
                break;
            case SCOOTER:
                f.getProperties().put("marker-symbol", "scooter");
                f.getProperties().put("marker-color", "#3309ce");
                break;
        }
        return f;
    }

    public static Feature createBoatStopFeature(Point point) {
        return GeoJSONUtils.createPointFeature(point, "harbor", "#121daf");
    }

    public static Feature createPointFeature(Point point) {
        return createPointFeature(point, null, null);
    }

    public static Feature createPointFeature(Point point, String symbol) {
        return createPointFeature(point, symbol, null);
    }

    public static Feature createStartingPointFeature(Point p) {
        return GeoJSONUtils.createPointFeature(p, "arrow", "#07694c");
    }

    public static Feature createDestinationPointFeature(Point p) {
        return GeoJSONUtils.createPointFeature(p, "circle-stroked", "#bd0000");
    }

    public static Feature createPointFeature(Point p, String symbol, String color) {
        Feature f = new Feature();
        f.setGeometry(new gr.iccs.smart.mobility.geojson.Point(p.y(), p.x()));

        if (symbol != null) {
            f.getProperties().put("marker-symbol", symbol);
        }
        if (color != null) {
            f.getProperties().put("marker-color", color);
        }
        return f;
    }

    public static Feature createLine(Point a, Point b) {
        Feature f = new Feature();
        LineString line = new LineString();
        line.add(new LngLatAlt(a.y(), a.x()));
        line.add(new LngLatAlt(b.y(), b.x()));
        f.setGeometry(line);
        return f;
    }
}
