package gr.iccs.smart.mobility.vehicle;

import java.util.List;

public record VehicleListResponse(List<VehicleDTO> vehicles) implements VehicleResponse {
}
