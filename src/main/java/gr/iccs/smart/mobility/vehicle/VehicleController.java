package gr.iccs.smart.mobility.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.geojson.FeatureCollection;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    @GetMapping(value = { "", "/" })
    public ResponseEntity<?> getAll(@RequestParam(required = false, defaultValue = "json") String format) {
        log.debug("Vehicle API: Get All, format=" + format);
        if (format.toLowerCase().equals("geojson")) {
            var geoJsonResponse = vehicleService.createGeoJSON();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/geo+json"))
                    .body(geoJsonResponse);
        }
        var jsonResponse = vehicleService.getAll();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }

    @GetMapping(value = "/{id}")
    public VehicleDTO getById(@PathVariable String id) {
        log.debug("Vehicle API: Get By ID " + id);
        return VehicleDTO.fromVehicle(vehicleService.getById(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public void create(@Valid @RequestBody Vehicle vehicle) {
        log.debug("Vehicle API: Create " + vehicle);
        vehicleService.create(vehicle);
    }

    @PutMapping(value = "/{id}")
    public VehicleDTO updateStatus(@PathVariable String id, @RequestBody VehicleInfoDTO vehicle) {
        log.debug("Vehicle API: updateStatus for" + id + " with data " + vehicle);
        return VehicleDTO.fromVehicle(vehicleService.updateVehicleStatus(id, vehicle));
    }

    @GetMapping(value = "/geojson")
    public FeatureCollection generateGeoJSON() {
        return vehicleService.createGeoJSON();
    }
}
