package gr.iccs.smart.mobility.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.iccs.smart.mobility.location.LocationDTO;

public record ScooterDTO(
        @JsonProperty("scooter_id") String id,
        LocationDTO location,
        @JsonProperty("battery") Battery battery,
        @JsonProperty("is_dummy") Boolean dummy) {

    public Scooter toScooter() {
        var scooter = new Scooter(id(), VehicleType.SCOOTER, dummy(), null);
        scooter.setBattery(battery.level());
        scooter.setLocation(location().toPoint());
        return scooter;
    }
}
