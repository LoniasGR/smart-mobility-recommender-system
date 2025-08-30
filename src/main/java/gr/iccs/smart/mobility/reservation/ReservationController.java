package gr.iccs.smart.mobility.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reserve")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<String> reserve(@RequestBody ReservationInputDTO input) {
        try {
            reservationService.reserve(input);
            return ResponseEntity.ok().build();
        } catch (ReservationException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @PostMapping("/{username}/cancel/{vehicleId}")
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
