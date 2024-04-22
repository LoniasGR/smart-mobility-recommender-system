package gr.iccs.smart.mobility.location;

import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;

public record LocationDTO(Double latitude, Double longitude) {
    public static LocationDTO fromGeographicPoint(Point point) {
        if (point == null) {
            return null;
        }
        return new LocationDTO(point.x(), point.y());
    }

    public Point toPoint() {
        return Values.point(4326, latitude(), longitude()).asPoint();
    }

    public static IstanbulLocation istanbulLocation(Point point) {
        LocationDTO location = LocationDTO.fromGeographicPoint(point);
        LocationDTO first = new LocationDTO(41.1227, 29.0726);
        LocationDTO second = new LocationDTO(41.0963, 29.0532);
        switch (comparePointAndLine(location, first, second)) {
            case 1:
                return IstanbulLocation.ASIAN_SIDE;
            case 0:
                return IstanbulLocation.SEA;
            case -1:
                return IstanbulLocation.EUROPEAN_SIDE;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static int comparePointAndLine(LocationDTO point, LocationDTO lineStart, LocationDTO lineEnd) {
        // m = (y2 - y1)/(x2 - x1)
        double slope = (lineEnd.longitude() - lineStart.longitude()) / (lineEnd.latitude() - lineStart.latitude());

        // b = y1 - m x1
        double b = lineStart.longitude() - slope * lineStart.latitude();

        // Line is y = m x + b
        double longitudeInLine = slope * point.latitude() + b;

        double distance = point.longitude() - longitudeInLine;
        if (distance < 0) {
            return 1;
        } else if (distance == 0) {
            return 0;
        } else {
            return -1;
        }

    }
}
