package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CarWrapper {
    @JsonProperty("passenger_cars")
    private List<CarDTO> cars;

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public List<CarDTO> getCars() {
        return this.cars;
    }

    public void setCars(List<CarDTO> cars) {
        this.cars = cars;
    }
}
