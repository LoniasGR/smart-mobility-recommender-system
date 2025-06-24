package gr.iccs.smart.mobility.vehicle;

import gr.iccs.smart.mobility.geojson.FeatureCollection;

public record VehicleGeoJsonResponse(FeatureCollection geoJson) implements VehicleResponse {
}
