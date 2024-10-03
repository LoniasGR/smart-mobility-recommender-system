package gr.iccs.smart.mobility.connection;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface ConnectionRepository extends Neo4jRepository<Connection, String> {

    @Query("MATCH ()-[r:CONNECTS_TO]->() DETACH DELETE r")
    void deleteAllConnections();
}
