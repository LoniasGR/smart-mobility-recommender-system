# Recommendation Service for Smart Mobility Use Cases

## Description

## Requirements

* Java 21
* Docker
* Postman

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

## Database credentials

There are default database credentials set in the `application.properties` file, as well as in the container definition 
in `docker-compose.yml`. These are:
```
username: neo4j
password: develop-colombo-clara-sponsor-erosion-6500
```

## Postman Collection

Inside the `api/` folder, there is a Postman collection included. This can be imported into the postman app
to try out the different endpoints. They are divided by category and should be relatively easy to navigate, since
examples are provided. 

In the future, this could be moved into the OpenAPI specification.