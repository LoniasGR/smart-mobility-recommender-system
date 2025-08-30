package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.location.LocationDTO;

public record RecommendationRouteDTO(
                String username,
                LocationDTO origin,
                LocationDTO destination,
                RecommendationRequestOptionsDTO options) {

}
