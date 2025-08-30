package gr.iccs.smart.mobility.user;

import java.io.Serializable;
import java.time.LocalDate;

public record UserDAO(
        String username,
        LocalDate dateOfBirth,
        Gender gender) implements Serializable {
    public User toUser() {
        var user = new User(
                this.username(),
                null, null);

        user.setGender(this.gender());
        user.setDateOfBirth(this.dateOfBirth());
        return user;
    }
}
