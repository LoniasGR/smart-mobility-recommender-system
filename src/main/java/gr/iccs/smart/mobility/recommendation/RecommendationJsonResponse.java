package gr.iccs.smart.mobility.recommendation;

import java.util.List;

import gr.iccs.smart.mobility.vehicle.VehicleDTO;

public record RecommendationJsonResponse(List<List<VehicleDTO>> paths) implements RecommendationResponse {
}
