# Recommendation Service for Smart Mobility Use Cases

## Description

## Requirements

* Java 21
* Docker

## Running from source

1. If you have not already installed [Java 21](https://jdk.java.net/21/) or [Docker](https://docs.docker.com/engine/install/), go ahead
    and install them. 
2. Start Neo4j by running the included compose file with
    `docker compose up -d neo4j`.
3. Build the java binary by running `./mvnw package`. The compiled file can be found under
   `target/smart-mobility-demo-0.0.1-SNAPSHOT.jar`.
4. Run the java application with `java -jar target/smart-mobility-demo-0.0.1-SNAPSHOT.jar`.

## Running completely from docker

1. If haven't already installed Docker, go ahead and install it.
2. Start the neo4j and the application container by running `docker compose up -d`.
3. The application will be listening on port 8000 of your machine.
