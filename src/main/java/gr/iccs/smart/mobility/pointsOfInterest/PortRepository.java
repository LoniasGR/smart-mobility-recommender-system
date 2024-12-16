package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface PortRepository extends Neo4jRepository<Port, String> {
    List<Port> findByLocationNear(Point point);

    @Query("""
            MATCH p=(n:Port)-[*0..1]->(m)
                WHERE point.distance(n.location, $point) < $max
                RETURN n, collect(relationships(p)), collect(m)
                ORDER BY point.distance(n.location, $point) ASC
            """)
    List<Port> findByLocationNear(Point point, Distance max);

    Optional<Port> findByLocation(Point point);

    @Query("MATCH p=(n:Port)-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
    List<Port> getAllByOneLevelConnection();

    @Query("MATCH p=(n:Port{id: $portID})-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
    Port getOneByOneLevelConnection(String portID);

    // TODO: Investigate if we could do this with what the ORM offers alone
    @Query("MATCH (bs:Port{id: $portID})-[p:PARKED_IN]-(v:Vehicle{id: $vehicleID}) " +
            "DETACH DELETE p")
    public void deleteParkedIn(String portID, UUID vehicleID);
}