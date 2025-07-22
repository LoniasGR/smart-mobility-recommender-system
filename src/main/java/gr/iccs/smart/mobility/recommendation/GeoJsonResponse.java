package gr.iccs.smart.mobility.recommendation;

import java.util.List;

import gr.iccs.smart.mobility.geojson.FeatureCollection;

public record GeoJsonResponse(List<FeatureCollection> geoJsonList) implements RecommendationResponse {
}
