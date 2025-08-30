package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.reservation.Reservation;
import gr.iccs.smart.mobility.usage.Used;

import java.time.LocalDate;
import java.util.List;

public record UserDTO(
        String username,
        LocalDate dateOfBirth,
        Gender gender,
        List<Used> vehiclesUsed,
        List<Reservation> reservations) {
    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getVehiclesUsed(),
                user.getReservations());
    }

    public User toUser() {
        var user = new User(this.username(), this.vehiclesUsed(), this.reservations());
        user.setDateOfBirth(this.dateOfBirth());
        user.setGender(this.gender());
        return user;
    }
}
