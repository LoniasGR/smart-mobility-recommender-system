package gr.iccs.smart.mobility.vehicle;

import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends Neo4jRepository<Vehicle, UUID> {

    List<Vehicle> findByLocationNear(Point point, Distance max);

    List<Vehicle> findByLocationNear(Point point);
}
