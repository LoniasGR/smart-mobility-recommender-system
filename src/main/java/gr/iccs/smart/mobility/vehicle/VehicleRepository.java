package gr.iccs.smart.mobility.vehicle;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends Neo4jRepository<Vehicle, UUID> {

    List<Vehicle> findByLocationNear(Point point, Distance max);

    @Query("MATCH (v: Vehicle) WHERE v.type <> 'SEA_VESSEL' RETURN v " +
            "ORDER BY point.distance(v.location, $point) ASC LIMIT $limit;")
    List<Vehicle> findLandVesselsByLocationNear(Point point, Integer limit);

    @Query("MATCH (v:Vehicle)-[r:PARKED_IN]->(b:BoatStop{id: $uuid}) RETURN v")
    List<Vehicle> findSeaVesselsParkedInBoatStop(UUID uuid);
}
