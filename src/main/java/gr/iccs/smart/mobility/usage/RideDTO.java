package gr.iccs.smart.mobility.usage;

import gr.iccs.smart.mobility.location.LocationDTO;

public record RideDTO(String vehicleId, String username, LocationDTO location) {

}
