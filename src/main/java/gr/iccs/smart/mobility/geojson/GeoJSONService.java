package gr.iccs.smart.mobility.geojson;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import org.neo4j.driver.types.Point;
import org.springframework.stereotype.Service;

@Service
public class GeoJSONService {
    public Feature createVehicleFeature(Vehicle v) {
        Feature f = new Feature();
        f.getGeometry().getCoordinates().add(v.getLocation().y());
        f.getGeometry().getCoordinates().add(v.getLocation().x());
        f.getProperties().put("type", v.getType().toString());
        f.getProperties().put("id", v.getId().toString());
        switch (v.getType()) {
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

    public Feature createPointFeature(Point point, String symbol) {
        return createPointFeature(point, symbol, null);
    }
    public Feature createPointFeature(Point p, String symbol, String color) {
        Feature f = new Feature();
        f.getGeometry().getCoordinates().add(p.y());
        f.getGeometry().getCoordinates().add(p.x());
        f.getProperties().put("marker-symbol", symbol);
        if(color != null) {
            f.getProperties().put("marker-color", color);
        }
        return f;
    }
}
