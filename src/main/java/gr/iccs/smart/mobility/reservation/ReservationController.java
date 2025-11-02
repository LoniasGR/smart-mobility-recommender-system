package gr.iccs.smart.mobility.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reserve")
@Tag(name = "Reservations", description = "Manage vehicle reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create reservation", description = "Create a new vehicle reservation (can include multiple vehicles)", tags = { "Reservations" })
    public ResponseEntity<String> reserve(@RequestBody ReservationInputDTO input) {
        try {
            reservationService.reserve(input);
            return ResponseEntity.ok().build();
        } catch (ReservationException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @PostMapping("/{username}/cancel/{vehicleId}")
    @Operation(summary = "Cancel reservation", description = "Cancel a vehicle reservation", tags = { "Reservations" })
    public ResponseEntity<String> cancelReservation(@PathVariable String vehicleId,
            @PathVariable String username) {
        try {
            reservationService.cancelReservation(username, vehicleId);
            return ResponseEntity.ok().build();
        } catch (ReservationException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

}
