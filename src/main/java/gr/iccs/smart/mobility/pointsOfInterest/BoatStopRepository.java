package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface BoatStopRepository extends Neo4jRepository<BoatStop, UUID> {
    List<BoatStop> findByLocationNear(Point point);

    @Query("""
            MATCH p=(n:BoatStop)-[*0..1]->(m)
                WHERE point.distance(n.location, $point) < $max
                RETURN n, collect(relationships(p)), collect(m)
                ORDER BY point.distance(n.location, $point) ASC
            """)
    List<BoatStop> findByLocationNear(Point point, Distance max);

    Optional<BoatStop> findByLocation(Point point);

    @Query("MATCH p=(n:BoatStop)-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
    List<BoatStop> getAllByOneLevelConnection();

    @Query("MATCH p=(n:BoatStop{id: $boatStopID})-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
    BoatStop getOneByOneLevelConnection(UUID boatStopID);

    // TODO: Investigate if we could do this with what the ORM offers alone
    @Query("MATCH (bs:BoatStop{id: $boatStopID})-[p:PARKED_IN]-(v:Vehicle{id: $vehicleID}) " +
            "DETACH DELETE p")
    public void deleteParkedIn(UUID boatStopID, UUID vehicleID);
}