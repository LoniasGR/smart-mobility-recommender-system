package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.vehicle.VehicleDTO;

public record RecommendationDTO(VehicleDTO vehicle, LocationDTO destination) {
}
