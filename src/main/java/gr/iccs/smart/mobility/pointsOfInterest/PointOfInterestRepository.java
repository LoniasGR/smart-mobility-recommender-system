package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;
import java.util.Optional;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface PointOfInterestRepository extends Neo4jRepository<PointOfInterest, String> {
    @Query("MATCH (n:Port) RETURN n")
    List<Port> findAllPorts();

    List<Port> findByLocationNear(Point point);

    @Query("""
            MATCH p=(n:Port)-[*0..1]->(m)
                WHERE point.distance(n.location, $point) < $max
                RETURN n, collect(relationships(p)), collect(m)
                ORDER BY point.distance(n.location, $point) ASC
            """)
    List<Port> findPortsByLocationNear(Point point, Distance max);

    @Query("""
            MATCH p=(n:BusStop)-[*0..1]->(m)
                WHERE point.distance(n.location, $point) < $max
                RETURN n, collect(relationships(p)), collect(m)
                ORDER BY point.distance(n.location, $point) ASC
            """)
    List<BusStop> findBusStopsByLocationNear(Point point, Distance max);

    Optional<Port> findByLocation(Point point);

    @Query("MATCH p=(q)-[:PARKED_IN*0..1]->(n:Port)-[*0..1]->(m) RETURN collect(q), n, collect(relationships(p)), collect(m)")
    List<Port> getAllPortsByOneLevelConnection();

    @Query("MATCH p=(q)-[:PARKED_IN*0..1]->(n:Port{id: $portID})-[*0..1]->(m) RETURN collect(q), n, collect(relationships(p)), collect(m)")
    Port getOnePortByOneLevelConnection(String portID);

    @Query("MATCH p=(n:BusStop)-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
    List<BusStop> getAllBusStopsByOneLevelConnection();

    @Query("MATCH p=(n:BusStop{id: $busStopID})-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
    BusStop getOneBusStopByOneLevelConnection(String busStopID);

    // TODO: Investigate if we could do this with what the ORM offers alone
    @Query("MATCH (bs:Port{id: $portID})-[p:PARKED_IN]-(v:Vehicle{id: $vehicleID}) " +
            "DETACH DELETE p")
    public void deleteParkedIn(String portID, String vehicleID);
}