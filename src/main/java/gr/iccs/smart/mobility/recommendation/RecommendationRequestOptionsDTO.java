package gr.iccs.smart.mobility.recommendation;

import java.util.List;

import gr.iccs.smart.mobility.graph.WeightType;
import gr.iccs.smart.mobility.vehicle.VehicleType;

public record RecommendationRequestOptionsDTO(
                List<VehicleType> ignoreTypes, Integer recommendationPaths, WeightType weightType) {

}
