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

import gr.iccs.smart.mobility.util.FormatSelection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Retrieve and manage vehicles")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;
    private final VehicleDBService vehicleDBService;
    private final VehicleUtilitiesService vehicleUtilitiesService;

    VehicleController(VehicleService vehicleService, VehicleDBService vehicleDBService,
            VehicleUtilitiesService vehicleUtilitiesService) {
        this.vehicleService = vehicleService;
        this.vehicleDBService = vehicleDBService;
        this.vehicleUtilitiesService = vehicleUtilitiesService;
    }

    /**
     * @return A list of all vehicles in the system, either in JSON or GeoJSON
     *         format.
     */
    @GetMapping(value = { "" })
    @Operation(summary = "Get all vehicles", description = "Retrieve a list of all vehicles", tags = { "Vehicles" })
    public ResponseEntity<?> getAll(
            @RequestParam(required = false, defaultValue = "json") FormatSelection format) {
        log.debug("Vehicle API: Get All, format={}", format);
        if (format == FormatSelection.GEOJSON) {
            var geoJsonResponse = vehicleUtilitiesService.createGeoJSON();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/geo+json"))
                    .body(geoJsonResponse);
        }
        var jsonResponse = vehicleDBService.getAll();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve a vehicle by its ID", tags = { "Vehicles" })
    public VehicleDTO getById(@PathVariable String id) {
        log.debug("Vehicle API: Get By ID {}", id);
        return VehicleDTO.fromVehicle(vehicleDBService.getById(id));
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create vehicle", description = "Create a new vehicle", tags = { "Vehicles" })
    public void create(@Valid @RequestBody VehicleDAO vehicle) {
        log.info("Vehicle API: Create {}", vehicle);
        vehicleService.initialize(vehicle);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update vehicle status", description = "Update the status (battery, location, usage state) of an existing vehicle", tags = {
            "Vehicles" })
    public VehicleDTO updateStatus(@PathVariable String id, @RequestBody VehicleInfoDTO vehicle) {
        if(vehicle == null || vehicle.isEmpty()) {
            throw new BadVehicleRequest("Vehicle info data must be provided");
        }
        log.info("Vehicle API: updateStatus for {} with data {}", id, vehicle);
        return VehicleDTO.fromVehicle(vehicleService.updateVehicleInfo(id, vehicle));
    }
}
