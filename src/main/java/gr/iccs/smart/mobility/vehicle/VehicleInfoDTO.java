package gr.iccs.smart.mobility.vehicle;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gr.iccs.smart.mobility.location.LocationDTO;

public record VehicleInfoDTO(Battery battery, LocationDTO location, VehicleStatus status) implements Serializable {
    
    @JsonIgnore
    public boolean isEmpty() {
        return battery == null && (location == null || location.isEmpty()) && status == null;
    }
}
