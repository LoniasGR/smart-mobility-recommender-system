package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.location.LocationDTO;

public record RecommendationRouteDTO(
                LocationDTO startingLocation,
                LocationDTO endingLocation,
                RecommendationRequestOptionsDTO options) {

}
