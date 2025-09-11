package gr.iccs.smart.mobility.user;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    @Query("""
            MATCH p=(u:User)-[*0..1]->(m)
            WHERE u.id = $username
            RETURN u, collect(relationships(p)), collect(m)
                """)
    public Optional<User> findByUsername(String username);

    @Query("""
            MATCH (u:User)-[r:RESERVATION]->(v:Vehicle)
            WHERE u.id = $username AND v.id = $vehicleId
            DETACH DELETE r
            """)
    public void deleteReservation(String username, String vehicleId);

    @Query("""
            MATCH (u:User)-[r:RESERVATION]->(v:Vehicle)
            WHERE v.id = $vehicleId
            RETURN u
                """)
    public User findReserverOfVehicle(String vehicleId);

}
