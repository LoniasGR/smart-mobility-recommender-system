package gr.iccs.smart.mobility.scenario;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.iccs.smart.mobility.pointsOfInterest.PortDTO;
import gr.iccs.smart.mobility.vehicle.BoatDTO;
import gr.iccs.smart.mobility.vehicle.CarDTO;
import gr.iccs.smart.mobility.vehicle.ScooterDTO;

public record ScenarioDTO(List<PortDTO> ports,
                @JsonProperty("passenger_cars") List<CarDTO> cars,
                List<ScooterDTO> scooters,
                @JsonProperty("sea_vessels") List<BoatDTO> boats) {

}
