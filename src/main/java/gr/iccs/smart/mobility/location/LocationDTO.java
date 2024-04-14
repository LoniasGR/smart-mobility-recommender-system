package gr.iccs.smart.mobility.location;

import org.springframework.data.neo4j.types.GeographicPoint2d;

public record LocationDTO(Double latitude, Double longitude) {
    public static LocationDTO fromGeographicPoint(GeographicPoint2d point) {
        if (point == null) {
            return null;
        }
        return new LocationDTO(point.getLatitude(), point.getLongitude());
    }
}
