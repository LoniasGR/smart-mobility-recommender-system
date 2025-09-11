package gr.iccs.smart.mobility.usage;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.reservation.ReservationService;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.VehicleDBService;

@RestController
@RequestMapping("/api/ride")
public class RideController {
    private static final Logger log = LoggerFactory.getLogger(RideController.class);

    private final UserService userService;
    private final VehicleDBService vehicleDBService;
    private final ReservationService reservationService;

    RideController(UserService userService, VehicleDBService vehicleDBService, ReservationService reservationService) {
        this.userService = userService;
        this.vehicleDBService = vehicleDBService;
        this.reservationService = reservationService;
    }

    @PostMapping("start")
    public void startRide(@RequestBody RideDTO rideDTO) {
        if (userService.rideStatus(rideDTO.username()).isPresent()) {
            throw new RideException("The user is already on a ride");
        }
        var vehicle = vehicleDBService.getByIdNoConnections(rideDTO.vehicleId());

        if (vehicle.isInUse() || vehicle.isOutOfService()) {
            throw new RideException("The vehicle is unavailable");
        }

        if (vehicle.isReserved()) {
            var reserver = userService.findReserverOfVehicle(vehicle.getId());
            if (!reserver.getUsername().equals(rideDTO.username())) {
                throw new RideException("The vehicle is reserved by another user");
            }
            reservationService.deleteReservation(reserver.getUsername(), vehicle.getId());
        }
        var location = rideDTO.location() == null ? LocationDTO.fromGeographicPoint(vehicle.getLocation())
                : rideDTO.location();
        var time = LocalDateTime.now();
        log.info("Starting ride for user {} on vehicle {} at {}",
                rideDTO.username(), rideDTO.vehicleId(), time);
        UseDTO usageStart = new UseDTO(vehicle, UseStatus.ACTIVE,
                location, time);
        userService.manageRide(rideDTO.username(), usageStart);
    }

    @PostMapping("end")
    public void endRide(@RequestBody RideDTO rideDTO) {
        var userCurrentRide = userService.rideStatus(rideDTO.username());
        if (!userCurrentRide.isPresent()) {
            throw new RideException("The user is not on a ride");
        }

        var vehicle = vehicleDBService.getByIdNoConnections(rideDTO.vehicleId());

        if (!vehicle.isInUse()) {
            throw new RideException("The vehicle is not part of a ride");
        }

        if (!userCurrentRide.get().getVehicle().getId().equals(vehicle.getId())) {
            throw new RideException("The user is not riding this vehicle");
        }

        var time = LocalDateTime.now();
        var location = rideDTO.location() == null ? LocationDTO.fromGeographicPoint(vehicle.getLocation())
                : rideDTO.location();
        log.info("Ending ride for user {} on vehicle {} at {}",
                rideDTO.username(), rideDTO.vehicleId(), time);
        UseDTO usageEnd = new UseDTO(vehicle, UseStatus.COMPLETED,
                location, time);
        userService.manageRide(rideDTO.username(), usageEnd);
    }
}
