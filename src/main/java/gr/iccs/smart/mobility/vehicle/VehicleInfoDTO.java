package gr.iccs.smart.mobility.vehicle;

import java.io.Serializable;

import gr.iccs.smart.mobility.location.LocationDTO;

public record VehicleInfoDTO(Battery battery, LocationDTO location, VehicleStatus status) implements Serializable {

}
