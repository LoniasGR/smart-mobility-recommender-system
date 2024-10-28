package gr.iccs.smart.mobility.recommendation;

import org.neo4j.driver.types.Point;

import gr.iccs.smart.mobility.location.InvalidLocationException;
import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.location.LocationDTO;

public class RecommendationUtils {
    protected static Boolean areSameIstanbulSide(Point startingPoint, Point finishingPoint) {
        var startingPointPosition = LocationDTO.istanbulLocation(startingPoint);
        var finishingPointPosition = LocationDTO.istanbulLocation(finishingPoint);

        if (startingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA
                || finishingPointPosition == IstanbulLocations.IstanbulLocationDescription.SEA) {
            throw new InvalidLocationException("The starting and ending locations must not be on the sea");
        }
        return startingPointPosition == finishingPointPosition;
    }
}
