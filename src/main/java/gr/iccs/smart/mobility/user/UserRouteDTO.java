package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.location.LocationDTO;

public record UserRouteDTO(LocationDTO startingLocation, LocationDTO endingLocation) {

}
