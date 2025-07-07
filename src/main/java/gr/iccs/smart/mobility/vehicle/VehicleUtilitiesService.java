package gr.iccs.smart.mobility.vehicle;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.iccs.smart.mobility.config.DataFileConfig;
import gr.iccs.smart.mobility.connection.ConnectionService;
import gr.iccs.smart.mobility.connection.ReachableNode;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import gr.iccs.smart.mobility.util.ResourceReader;

@Service
public class VehicleUtilitiesService {

    private static final Logger log = LoggerFactory.getLogger(VehicleUtilitiesService.class);

    private final DataFileConfig dataFileConfig;
    private final ResourceReader resourceReader;
    private final VehicleDBService vehicleDBService;
    private final ConnectionService connectionService;

    public VehicleUtilitiesService(DataFileConfig dataFileConfig, ResourceReader resourceReader,
            VehicleDBService vehicleDBService, ConnectionService connectionService) {
        this.dataFileConfig = dataFileConfig;
        this.resourceReader = resourceReader;
        this.vehicleDBService = vehicleDBService;
        this.connectionService = connectionService;
    }

    public CarWrapper createCarsFromResourceFile() {
        String filePath = dataFileConfig.getCarLocations();
        try {
            ObjectMapper mapper = new ObjectMapper();
            var stream = resourceReader.readResource(filePath);
            return mapper.readValue(stream, CarWrapper.class);
        } catch (FileNotFoundException e) {
            log.warn("File {} not found, terminating...", filePath);
            throw new RuntimeException("File not found: " + filePath, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FeatureCollection createGeoJSON() {
        FeatureCollection geoJSON = new FeatureCollection();
        geoJSON = addVehiclesToGeoJSON(geoJSON);
        return geoJSON;
    }

    public FeatureCollection addVehiclesToGeoJSON(FeatureCollection fc) {
        var vehicles = vehicleDBService.getAll();
        for (var v : vehicles) {
            fc.getFeatures().add(GeoJSONUtils.createVehicleFeature(v));
        }
        return fc;
    }

    public LandVehicle createConnectionTo(LandVehicle vehicle, ReachableNode destination, Double maxDistance) {
        var connection = connectionService.generateConnection(vehicle, destination);
        if (maxDistance != null && connection.getDistance() > maxDistance) {
            return vehicle;
        }
        vehicle.addConnection(connection);
        return vehicle;
    }
}
