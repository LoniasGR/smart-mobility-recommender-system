package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.location.LocationDTO;

public record RecommendationRouteDTO(
        LocationDTO origin,
        LocationDTO destination,
        RecommendationRequestOptionsDTO options) {

}
