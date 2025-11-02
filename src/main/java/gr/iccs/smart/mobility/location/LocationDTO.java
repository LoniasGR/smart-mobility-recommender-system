package gr.iccs.smart.mobility.location;

import java.io.Serializable;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;

public record LocationDTO(Double latitude, Double longitude) implements Serializable {
    public static LocationDTO fromGeographicPoint(Point point) {
        if (point == null) {
            return null;
        }
        return new LocationDTO(point.x(), point.y());
    }

    public Point toPoint() {
        return Values.point(4326, latitude(), longitude()).asPoint();
    }

    public boolean isEmpty() {
        return latitude() == null || longitude() == null;
    }
}
