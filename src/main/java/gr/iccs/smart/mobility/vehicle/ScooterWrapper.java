package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScooterWrapper {
    @JsonProperty("scooters")
    private List<ScooterDTO> scooters;

    public List<ScooterDTO> getScooters() {
        return this.scooters;
    }

    public void setScooters(List<ScooterDTO> scooters) {
        this.scooters = scooters;
    }

}
