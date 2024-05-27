package gr.iccs.smart.mobility.scenario;

import gr.iccs.smart.mobility.location.LocationDTO;

import java.util.UUID;

public record UseScenarioDTO(
        UUID vehicleID,
        LocationDTO location
) {
}
