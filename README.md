# Recommendation Service for Smart Mobility Use Cases

## Description

This is a recommendation service for the EcoMobility project. It provides routing services for
individuals around the city of Istanbul, using the vehicle types developed for the project. The functionality is
exposed through a REST API, which currently does not implement any authentication methods.

## Requirements

* Java 21
* Docker

## Application requirements

* Neo4J: The application uses Neo4J as its database.
* openrouteservice: openroutservice is used as the source of paths for the different vehicles.

## Setup

### Setting up Neo4J

1. Install Docker to run the local Neo4J database.
2. To set up the database, the only thing needed is defining the password. To do so, copy `.env.example` to `.env` and set up the database password.
3. Run `docker compose up -d neo4j` to start the database. 

### Setting up openrouteservice

1. Create the folder `ors-docker` if not existing and its subfolders.

   ```bash
   mkdir -p ors-docker/config ors-docker/elevation_cache ors-docker/graphs ors-docker/files ors-docker/logs
   ```
2. In the `files` subfolder add the map of the area you are interested in (i.e. Turkey is [this file](https://download.geofabrik.de/europe/turkey-latest.osm.pbf)).
3. In the `docker-compose.yaml` change `ors.engine.profile_default.build.source_file` value to the location of your map. To make the preprocessing faster, you can disable some of the profiles in the config. The required ones for the application are enabled by default.
4. Start the container `docker compose up -d openrouteservice`
5. Wait for it to preprocess the map (this can take a while)

### Running from source

1. If you have not already installed [Java 21](https://jdk.java.net/21/), go ahead and install it.
2. Copy `src/resoures/application.yml.example` to `src/resources/application.yml`. Make sure the password on the `.env` file and `src/resources/application.yml` match.
3. Build the java binary by running `./mvnw package`. The compiled file can be found under
   `target/smart-mobility-demo-0.0.1-SNAPSHOT.jar`.
4. Run the java application with `java -jar target/smart-mobility-demo-0.0.1-SNAPSHOT.jar`.

## Running completely from docker

Instead of running every service separately, you can set them up to run all together. 

1. Follow steps 1 & 2 for setting up the Neo4J database.
2. Follow steps 1-3 for setting up the openrouteservice.
3. Copy `resoures/application.yaml.example` to `resources/application.yaml`
4. Start neo4j, openroutservice and the application container by running `docker compose up -d`.
5. The application will be listening on port 8000 of your machine.

## Database credentials

There are default database credentials set in the `application.yml` file, as well as in the container definition
in `.env`. These are:

```yaml
username: neo4j
password: develop-colombo-clara-sponsor-erosion-6500
```

## API Routes

### OpenAPI Documentation

The application serves its OpenAPI documentation at [http://localhost:8000/v3/api-docs](http://localhost:8000/v3/api-docs). Make sure to modify the host and port according to your setup. 

### Postman Collection

Inside the `api/` folder, there is a Postman collection included. This can be imported into the postman app
to try out the different endpoints. They are divided by category and should be relatively easy to navigate, since
examples are provided.

In the future, this could be moved into the OpenAPI specification.
