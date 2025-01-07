package gr.iccs.smart.mobility.scenario;

import java.util.List;

import gr.iccs.smart.mobility.pointsOfInterest.PortDTO;
import gr.iccs.smart.mobility.vehicle.BoatDTO;
import gr.iccs.smart.mobility.vehicle.CarDTO;
import gr.iccs.smart.mobility.vehicle.ScooterDTO;

public record ScenarioDTO(List<PortDTO> ports, List<CarDTO> cars, List<ScooterDTO> scooters, List<BoatDTO> boats) {

}
