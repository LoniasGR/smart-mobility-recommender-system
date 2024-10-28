package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<VehicleDTO> getAll() {
        log.debug("Vehicle API: Get All");
        return vehicleService.getAll();
    }

    @GetMapping(value = "/{id}")
    public VehicleDTO getById(@PathVariable UUID id) {
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
    public VehicleDTO updateStatus(@PathVariable UUID id, @RequestBody VehicleInfoDTO vehicle) {
        log.debug("Vehicle API: updateStatus for" + id + " with data " + vehicle);
        return VehicleDTO.fromVehicle(vehicleService.updateVehicleStatus(id, vehicle));
    }

    @GetMapping(value = "/geojson")
    public FeatureCollection generateGeoJSON() {
        return vehicleService.createGeoJSON();
    }
}
