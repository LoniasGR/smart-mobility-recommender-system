package gr.iccs.smart.mobility.reservation;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import gr.iccs.smart.mobility.vehicle.Vehicle;

@RelationshipProperties
public class Reservation implements Serializable {
    @RelationshipId
    private String id;

    private final LocalDateTime reservationTime;

    @TargetNode
    private final Vehicle vehicle;

    public Reservation(Vehicle vehicle, LocalDateTime reservationTime) {
        this.vehicle = vehicle;
        this.reservationTime = reservationTime;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}