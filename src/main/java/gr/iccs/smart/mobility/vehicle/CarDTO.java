package gr.iccs.smart.mobility.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.iccs.smart.mobility.location.LocationDTO;

public record CarDTO(
        @JsonProperty("car_id") String id,
        LocationDTO location,
        @JsonProperty("battery") Battery battery,
        @JsonProperty("is_dummy") Boolean dummy) {

    public Car toCar() {
        var car = new Car(id(), VehicleType.CAR, dummy(), null);
        car.setBattery(battery.level());
        car.setLocation(location().toPoint());
        return car;
    }
}
