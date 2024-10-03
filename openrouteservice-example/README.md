# openrouteservice example

## Requirements

- Docker

## Steps

- Create the folder `ors-docker` if not existing and its subfolders.

  ```
  mkdir -p ors-docker/config ors-docker/elevation_cache ors-docker/graphs ors-docker/files ors-docker/logs
  ```

- In the `config` subfolder add the `ors-config.yml`. A sample config can be found in [Github](https://github.com/GIScience/openrouteservice/blob/main/ors-config.yml)
- In the `files` subfolder add the map of the area you are interested in (i.e. Turkey is [this file](https://download.geofabrik.de/europe/turkey-latest.osm.pbf))
- Configure the `ors-config.yml` and especially the `engine -> source_file` property. The path should be something like `/home/ors/files/map.osm.` To make the preprocessing faster, you can disable some of the profiles in the config.
- Start the container `docker compose up`
- Wait for it to preprocess the map (this can take a while)
- Import the postman collection in postman.
- Tryout some examples.
