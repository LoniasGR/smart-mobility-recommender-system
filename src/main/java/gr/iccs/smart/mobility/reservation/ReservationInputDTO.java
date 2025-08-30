package gr.iccs.smart.mobility.reservation;

import java.util.List;

public record ReservationInputDTO(String username, List<String> vehicleIds) {

}
