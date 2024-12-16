package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.UUID;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface VehicleRepository extends Neo4jRepository<Vehicle, UUID> {

        List<Vehicle> findByLocationNear(Point point, Distance max);

        @Query("MATCH (v: LandVehicle) RETURN v")
        <T> List<T> getAllLandVehicles(Class<T> type);

        @Query("MATCH p=(n:LandVehicle)-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
        List<LandVehicle> findAllLandVehiclesWithOneLevelConnection();

        @Query("MATCH p=(n:LandVehicle{id: $vehicleId})-[*0..1]->(m) RETURN n, collect(relationships(p)), collect(m)")
        LandVehicle findLandVehicleWithOneLevelConnection(UUID vehicleId);

        @Query("MATCH (v:Vehicle) RETURN v")
        List<VehicleDTO> findAllVehiclesNoConnections();

        @Query("MATCH p=(n:LandVehicle)-[*0..1]->(m) " +
                        "WHERE point.distance(n.location, $point) < $range " +
                        "RETURN n, collect(relationships(p)), collect(m) " +
                        "ORDER BY point.distance(n.location, $point) ASC;")
        List<LandVehicle> findLandVechicleWithOneLevelConnectionByLocationAround(Point point, Distance range);

        @Query("MATCH (v: LandVehicle) " +
                        "WHERE point.distance(v.location, $point) < $range RETURN v " +
                        "ORDER BY point.distance(v.location, $point) ASC;")
        List<LandVehicle> findLandVehicleNoConnectionByLocationAround(Point point, Distance range);

        @Query("MATCH (v: LandVehicle)" +
                        "WHERE point.distance(v.location, $point) < $range RETURN v " +
                        "ORDER BY point.distance(v.location, $point) ASC LIMIT $limit;")
        List<LandVehicle> findLandVesselsByLocationAround(Point point, Double range, Integer limit);

        @Query("MATCH (v: Vehicle{type: $type}) " +
                        "WHERE point.distance(v.location, $point) < $range RETURN v " +
                        "ORDER BY point.distance(v.location, $point) ASC LIMIT $limit")
        List<Vehicle> findVehicleByTypeAndLocationAround(String type, Point point, Double range, Integer limit);

        @Query("MATCH (v: Vehicle{type: $type}) RETURN v " +
                        "ORDER BY point.distance(v.location, $point) ASC LIMIT $limit")
        List<Vehicle> findVehicleByTypeAndLocationNear(String type, Point point, Integer limit);

        @Query("MATCH (v:Vehicle)-[r:PARKED_IN]->(b:Port{id: $uuid}) RETURN v")
        List<Vehicle> findSeaVesselsParkedInPort(UUID uuid);

}
