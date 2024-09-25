package gr.iccs.smart.mobility.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.iccs.smart.mobility.usage.UseStatus;
import gr.iccs.smart.mobility.usage.Used;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class User {

    @Id
    @Property("id")
    @NotNull(message = "username cannot be null")
    private final String username;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Relationship(type = "USED", direction = Relationship.Direction.OUTGOING)
    private final List<Used> vehiclesUsed;

    public User(@JsonProperty("username") String username, List<Used> vehiclesUsed) {
        this.username = username;
        this.vehiclesUsed = Objects.requireNonNullElseGet(vehiclesUsed, ArrayList::new);
    }

    public Optional<Used> getCurrentRide() {
        return vehiclesUsed.stream()
                .filter(r -> r.getStatus().equals(UseStatus.ACTIVE))
                .findFirst();
    }

    /*
     *****************************************************
     * GETTER & SETTER
     *****************************************************
     */

    public String getUsername() {
        return username;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<Used> getVehiclesUsed() {
        return vehiclesUsed;
    }
}
