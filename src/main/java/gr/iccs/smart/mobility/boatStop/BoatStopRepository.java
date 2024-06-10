package gr.iccs.smart.mobility.boatStop;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoatStopRepository extends Neo4jRepository<BoatStop, UUID> {
    List<BoatStop> findByLocationNear(Point point);
    Optional<BoatStop> findByLocation(Point point);

    // TODO: Investigate if we could do this with what the ORM offers alone
    @Query("MATCH (bs:BoatStop{id: $boatStopID})-[p:PARKED_IN]-(v:Vehicle{id: $vehicleID}) " +
            "DETACH DELETE p")
    public void deleteParkedIn(UUID boatStopID, UUID vehicleID);
}