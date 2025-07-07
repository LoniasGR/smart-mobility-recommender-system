package gr.iccs.smart.mobility.location;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;

import java.io.Serializable;
import java.util.List;

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

    public static IstanbulLocations.IstanbulLocationDescription istanbulLocation(Point point) {
        LocationDTO location = LocationDTO.fromGeographicPoint(point);
        return istanbulLocation(location);
    }

    public static IstanbulLocations.IstanbulLocationDescription istanbulLocation(LocationDTO location) {
        if (pointInPolygon(location, IstanbulLocations.europeanSidePolygon)) {
            return IstanbulLocations.IstanbulLocationDescription.EUROPEAN_SIDE;
        }

        if (pointInPolygon(location, IstanbulLocations.asianSidePolygon)) {
            return IstanbulLocations.IstanbulLocationDescription.ASIAN_SIDE;
        }
        return IstanbulLocations.IstanbulLocationDescription.SEA;
    }

    /**
     * <a href=
     * "https://observablehq.com/@tmcw/understanding-point-in-polygon">Source</a>
     */
    private static boolean pointInPolygon(LocationDTO point, List<LocationDTO> polygon) {
        Double x = point.latitude(), y = point.longitude();
        var inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            Double xi = polygon.get(i).latitude(), yi = polygon.get(i).longitude();
            Double xj = polygon.get(j).latitude(), yj = polygon.get(j).longitude();

            // If edge is above or below the point then they definitely don't intersect
            var intersect = ((yi > y) != (yj > y))
                    // Check if the point is left from the edge or not
                    // We are substituting the position of the point in the line
                    && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);

            if (intersect)
                inside = !inside;
        }
        return inside;
    }
}
