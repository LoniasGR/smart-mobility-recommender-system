package gr.iccs.smart.mobility.boatStop;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface BoatStopRepository extends Neo4jRepository<BoatStop, UUID> {

    List<BoatStop> findByLocationNear(Point point);
    Optional<BoatStop> findByLocation(Point point);
}