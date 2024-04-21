package gr.iccs.smart.mobility.location;

import org.neo4j.driver.types.Point;

public record LocationDTO(Double latitude, Double longitude) {
    public static LocationDTO fromGeographicPoint(Point point) {
        if (point == null) {
            return null;
        }
        return new LocationDTO(point.x(), point.y());
    }
}
