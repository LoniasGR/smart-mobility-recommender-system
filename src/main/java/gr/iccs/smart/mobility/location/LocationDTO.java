package gr.iccs.smart.mobility.location;

import java.io.Serializable;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    public boolean isEmpty() {
        return latitude() == null || longitude() == null;
    }
}
