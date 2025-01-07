package gr.iccs.smart.mobility.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.iccs.smart.mobility.location.LocationDTO;

public record BoatDTO(
        @JsonProperty("sea_vessel_id") String id,
        LocationDTO location,
        @JsonProperty("battery") Battery battery,
        @JsonProperty("is_dummy") Boolean dummy) {

    public Boat toBoat() {
        var boat = new Boat(id(), VehicleType.SEA_VESSEL, dummy(), null);
        boat.setBattery(battery.level());
        boat.setLocation(location().toPoint());
        return boat;
    }
}
