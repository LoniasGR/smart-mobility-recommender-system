# Recommendation Service for Smart Mobility Use Cases

## Table of contents
- [Recommendation Service for Smart Mobility Use Cases](#recommendation-service-for-smart-mobility-use-cases)
  - [Table of contents](#table-of-contents)
  - [Description](#description)
  - [Application dependencies](#application-dependencies)
  - [Installation](#installation)
    - [All-in-one installation](#all-in-one-installation)
    - [Setting up individual components separately](#setting-up-individual-components-separately)
      - [Setting up Neo4J](#setting-up-neo4j)
      - [Setting up openrouteservice](#setting-up-openrouteservice)
    - [Running from source](#running-from-source)
  - [Database credentials](#database-credentials)
  - [Usage](#usage)
    - [Swagger UI](#swagger-ui)
  - [API Routes](#api-routes)
    - [OpenAPI Documentation](#openapi-documentation)
    - [Postman Collection](#postman-collection)


## Description

This is a recommendation service for the EcoMobility project. It provides routing services for
individuals around the city of Istanbul, using the vehicle types developed for the project. The functionality is
exposed through a REST API, which currently does not implement any authentication methods.


## Application dependencies

* ***Neo4J***: The application uses Neo4J as its database.
* **openrouteservice**: openroutservice is used as the source of paths for the different vehicles.

## Installation

The best way to install the application and it's dependencies is 
with the docker compose file. This will deploy everything needed to run a local version of the smart-mobility application, i.e. a local Neo4J database, a local openrouteservice API and the smart-service application.

### All-in-one installation

1. In a terminal window, clone the repository and enter the directory.

   ```bash
   git clone https://github.com/LoniasGR/smart-mobility-recommender-system
   cd smart-mobility-recommender-system
   ```
2. Create your `.env` file by copying and adjusting the `.env.example` file.
   ```bash
   cp .env.example .env
   ```
   >  **IMPORTANT**: Do not use the default values in a production environment. At the very least make sure to change the database password.

3.  Create the folder `ors-docker/files` if it does not exist.
    ```bash
    mkdir -p ors-docker/files
    ```
4. Move the file of the area you are interested in (i.e. Turkey is [this file](https://download.geofabrik.de/europe/turkey-latest.osm.pbf)) to the `ors-docker/files` folder.
   ```bash
   wget https://download.geofabrik.de/europe/turkey-latest.osm.pbf -O ors-docker/files/turkey-latest.osm.pbf
   ```
5. Copy `src/main/resoures/application.yml.example` to `src/main/resources/application.yml`
   ```bash
   cp src/main/resoures/application.yml.example src/main/resources/application.yml
   ```
6. Start neo4j, openroutservice and the application containers by running `docker compose up -d`.
   > **IMPORTANT**: The openrouteservice will take some time to be available, depending on the size of the map file. You can adjust the settings in `docker-compose.yaml` to make it faster by disabling some profiles.
7. The application will be listening on http://localhost:8000. <br>
   The Swagger UI is located at http://localhost:8000/swagger-ui.html. <br>
   neo4J also provides a UI located at http://localhost:7474.

### Setting up individual components separately

Alternatively, if you want to only deploy specific components of the project, you can see what's needed for each one of them separately.

#### Setting up Neo4J

1. To set up the database, the only thing needed is defining the password. To do so, copy `.env.example` to `.env` and set up the database password.
2. Run `docker compose up -d neo4j` to start the database. 
3. You can visually inspect the database at http://localhost:7474.

#### Setting up openrouteservice

1. Create the folder `ors-docker` if not existing and at least the `files` subfolder.
   ```bash
   mkdir -p ors-docker/files
   ```
2. In the `files` subfolder add the map of the area you are interested in (i.e. Turkey is [this file](https://download.geofabrik.de/europe/turkey-latest.osm.pbf)).
   ```bash
   wget https://download.geofabrik.de/europe/turkey-latest.osm.pbf -O ors-docker/files/turkey-latest.osm.pbf
   ```
3. Start the container `docker compose up -d openrouteservice`
4. Wait for it to preprocess the map (this can take a while). You can check the logs of the `openrouteservice` container to verify that it's done. This can take around 30 mins based on the size of the map, and the compute resources of the 

### Running from source

1. If you have not already installed [Java 21](https://jdk.java.net/21/), go ahead and install it.
2. Copy `src/main/resoures/application.yml.example` to `src/main/resources/application.yml`. Make sure the password on the `.env` file and `src/resources/application.yml` match.
3. Build the java binary by running `./mvnw package`. The compiled file can be found under
   `target/smart-mobility-demo-0.0.1-SNAPSHOT.jar`.
4. Run the java application with `java -jar target/smart-mobility-demo-0.0.1-SNAPSHOT.jar`.

## Database credentials

There are default database credentials set in the `application.yml` file, as well as in the container definition
in `.env`. These are:

```yaml
username: neo4j
password: develop-colombo-clara-sponsor-erosion-6500
```

## Usage

After setting up the project, you can test the provided API by using either the built-in Swagger UI or the Postman collection. For brevity reasons only the SwaggerUI is documented below, but similar principles apply to the postman collection.

### Swagger UI

Navigate to http://localhost:8000/swagger-ui.html.

The database is initially empty, so you need to add vehicles, users, ports, etc. The easiest way to do that is through the scenario endpoints. In the `examples` directory of the project, there are examples that can be used. The `ports-vehicles.json` contains the scenario files, while the `users.json` contains a request for a route. 

Select the POST `api/scenario` from the scenario controller and add the respective `ports-vehicles.json` in the request body, while on the params add:
```json
// This resets the database in case it was already populated
{
   "force": true 
}
```

After that, you can request a recommendation path by using the `api/recommend/{username}` by using as username `test.user`, the other two options (`wholeMap`, `previewGraph`) set to `false` and as request body the `user.json` file.

## API Routes

### OpenAPI Documentation

The application serves its OpenAPI JSON file at [http://localhost:8000/v3/api-docs](http://localhost:8000/v3/api-docs), while the swagger ui can be found at [http://localhost:8000/swagger-ui.html](http://localhost:8000/swagger-ui.html). 

### Postman Collection

Inside the `api/` folder, there is a Postman collection included. This can be imported into the postman app
to try out the different endpoints. They are divided by category and should be relatively easy to navigate, since examples are provided.