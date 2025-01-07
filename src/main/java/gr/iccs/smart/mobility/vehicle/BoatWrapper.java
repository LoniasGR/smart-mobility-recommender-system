package gr.iccs.smart.mobility.vehicle;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoatWrapper {
    @JsonProperty("sea_vessels")
    private List<BoatDTO> boats;

    public List<BoatDTO> getBoats() {
        return this.boats;
    }

    public void setScooters(List<BoatDTO> boats) {
        this.boats = boats;
    }

}
