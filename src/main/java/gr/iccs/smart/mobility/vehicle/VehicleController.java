package gr.iccs.smart.mobility.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import gr.iccs.smart.mobility.util.FormatSelection;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    private VehicleService vehicleService;

    VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * @return A list of all vehicles in the system, either in JSON or GeoJSON
     *         format.
     */
    @GetMapping(value = { "", "/" })
    public ResponseEntity<VehicleResponse> getAll(
            @RequestParam(required = false, defaultValue = "json") FormatSelection format) {
        log.debug("Vehicle API: Get All, format={}", format);
        if (format == FormatSelection.GEOJSON) {
            var geoJsonResponse = vehicleService.createGeoJSON();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/geo+json"))
                    .body(new VehicleGeoJsonResponse(geoJsonResponse));
        }
        var jsonResponse = vehicleService.getAll();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new VehicleListResponse(jsonResponse));
    }

    @GetMapping(value = "/{id}")
    public VehicleDTO getById(@PathVariable String id) {
        log.debug("Vehicle API: Get By ID {}", id);
        return VehicleDTO.fromVehicle(vehicleService.getById(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    public void create(@Valid @RequestBody Vehicle vehicle) {
        log.debug("Vehicle API: Create {}", vehicle);
        vehicleService.create(vehicle);
    }

    @PutMapping(value = "/{id}")
    public VehicleDTO updateStatus(@PathVariable String id, @RequestBody VehicleInfoDTO vehicle) {
        log.debug("Vehicle API: updateStatus for {} with data {}", id, vehicle);
        return VehicleDTO.fromVehicle(vehicleService.updateVehicleStatus(id, vehicle));
    }

    @GetMapping(value = "/geojson")
    public FeatureCollection generateGeoJSON() {
        return vehicleService.createGeoJSON();
    }
}
