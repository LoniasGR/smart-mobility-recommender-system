package gr.iccs.smart.mobility.util;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class DBClientProvider {
    private Neo4jClient client;

    DBClientProvider(
            @Value("${spring.neo4j.uri}") String uri,
            @Value("${spring.neo4j.authentication.username}") String username,
            @Value("${spring.neo4j.authentication.password}") String password) {
        Driver driver = GraphDatabase
                .driver(uri, AuthTokens.basic(username, password));

        this.client = Neo4jClient.create(driver);
    }

    public Neo4jClient getClient() {
        return this.client;
    }
}
