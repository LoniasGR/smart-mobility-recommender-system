package gr.iccs.smart.mobility.vehicle;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.UUID;

public interface VehicleRepository extends Neo4jRepository<Vehicle, UUID> {

}
