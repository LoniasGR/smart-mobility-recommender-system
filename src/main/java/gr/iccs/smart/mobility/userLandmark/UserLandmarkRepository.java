package gr.iccs.smart.mobility.userLandmark;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface UserLandmarkRepository extends Neo4jRepository<UserLandmark, String> {

}
