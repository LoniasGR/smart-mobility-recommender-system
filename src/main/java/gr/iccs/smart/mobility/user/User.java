package gr.iccs.smart.mobility.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.iccs.smart.mobility.usage.Used;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.List;

public class User {

    @Id
    @Property("id")
    @NotNull(message = "username cannot be null")
    private final String username;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Version
    private Long version;

    @Relationship(type="USED", direction = Relationship.Direction.OUTGOING)
    private final List<Used> vehicles;

    public User(@JsonProperty("username") String username, List<Used> vehicles) {
        this.username = username;
        this.vehicles = vehicles;
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

    public List<Used> getVehicles() {
        return vehicles;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
