package gr.iccs.smart.mobility.geojson;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;
import org.neo4j.driver.types.Point;
import org.springframework.stereotype.Service;

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
                f.getProperties().put("marker-symbol", "racetrack-boat");
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

    public static Feature createPointFeature(Point point) {
        return createPointFeature(point, null, null);
    }

    public static Feature createPointFeature(Point point, String symbol) {
        return createPointFeature(point, symbol, null);
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
}
