package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.usage.Used;

import java.time.LocalDate;
import java.util.List;

public record UserDTO(
        String username,
        LocalDate dateOfBirth,
        Gender gender,
        List<Used> vehiclesUsed
) {
    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getUsername(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getVehiclesUsed()
        );
    }
}
