package gr.iccs.smart.mobility.reservation;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.user.PersonNotFoundException;
import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleDBService;
import gr.iccs.smart.mobility.vehicle.VehicleGraphService;
import gr.iccs.smart.mobility.vehicle.VehicleNotFoundException;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import gr.iccs.smart.mobility.vehicle.VehicleStatus;

@Service
public class ReservationService {

    private final UserService userService;
    private final VehicleDBService vehicleDbService;
    private final VehicleService vehicleService;
    private final VehicleGraphService vehicleGraphService;

    public ReservationService(UserService userService, VehicleDBService vehicleDbService,
            VehicleService vehicleService, VehicleGraphService vehicleGraphService) {
        this.userService = userService;
        this.vehicleDbService = vehicleDbService;
        this.vehicleGraphService = vehicleGraphService;
        this.vehicleService = vehicleService;
    }

    public void reserve(ReservationInputDTO input) {
        try {
            var user = userService.getById(input.username());
            var vehicles = input.vehicleIds().stream()
                    .map(vehicleDbService::getByIdNoConnections)
                    .toList();

            for (var vehicle : vehicles) {
                user.addReservation(createReservation(vehicle));
            }
            userService.update(user);

        } catch (PersonNotFoundException e) {
            throw new ReservationException("Person " + input.username() + "not found", HttpStatus.NOT_FOUND);
        } catch (VehicleNotFoundException e) {
            throw new ReservationException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public Optional<Vehicle> getVehicleReservation(String vehicleId) {
        return vehicleDbService.findVehicleReservation(vehicleId);
    }

    private Reservation createReservation(Vehicle vehicle) {
        if (!vehicle.isAvailable()) {
            throw new ReservationException("Vehicle " + vehicle.getId() + " is not available", HttpStatus.CONFLICT);
        }
        vehicleService.updateVehicleStatus(vehicle, VehicleStatus.RESERVED, true);
        vehicleGraphService.removeVehicleFromGraph(vehicle);
        return new Reservation(vehicle, LocalDateTime.now());
    }

    public void cancelReservation(String username, String vehicleId) {
        var v = deleteReservation(username, vehicleId);
        // We need to refetch the vehicle, so that Neo4J data recognizes it
        v = vehicleDbService.getByIdNoConnections(v.getId());
        vehicleGraphService.addVehicleToGraph(v);

    }

    public Vehicle deleteReservation(String username, String vehicleId) {
        var u = userService.getById(username);
        var reservation = u.getReservations().stream()
                .filter(r -> r.getVehicle().getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new ReservationException(
                        "No reservation found for user " + username + " and vehicle " + vehicleId,
                        HttpStatus.NOT_FOUND));
        var v = reservation.getVehicle();
        userService.deleteReservation(u, v);
        return v;
    }
}
