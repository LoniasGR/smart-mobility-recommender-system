package gr.iccs.smart.mobility.user;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface UserRepository extends Neo4jRepository<User, String> {

    @Query("""
        MATCH p=(u:User)-[*0..1]->(m) 
        WHERE u.id = $username 
        RETURN u, collect(relationships(p)), collect(m) 
            """)
    public Optional<User> findByUsername(String username);
}
