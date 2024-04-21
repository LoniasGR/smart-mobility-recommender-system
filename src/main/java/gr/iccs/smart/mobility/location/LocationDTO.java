package gr.iccs.smart.mobility.location;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.types.GeographicPoint2d;

public record LocationDTO(Double latitude, Double longitude) {
    public static LocationDTO fromGeographicPoint(Point point) {
        if (point == null) {
            return null;
        }
        return new LocationDTO(point.x(), point.y());
    }
}
