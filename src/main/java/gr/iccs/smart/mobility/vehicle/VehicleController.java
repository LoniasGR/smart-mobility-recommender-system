package gr.iccs.smart.mobility.vehicle;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping(value = {"", "/"})
    public List<VehicleDTO> getAll() {
        log.debug("Vehicle API: Get All");
        return vehicleService.getAll()
                .stream().map(VehicleDTO::fromVehicle)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{id}")
    public VehicleDTO getById(@PathVariable UUID id) {
        log.debug("Vehicle API: Get By ID " + id);
        return VehicleDTO.fromVehicle(vehicleService.getById(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@Valid @RequestBody Vehicle vehicle) {
        log.debug("Vehicle API: Create " + vehicle);
        vehicleService.create(vehicle);
    }

    @PutMapping(value = "/{id}")
    public VehicleDTO updateStatus(@PathVariable UUID id, @RequestBody VehicleInfoDTO vehicle) {
        log.debug("Vehicle API: updateStatus for" + id + " with data " + vehicle);
        return VehicleDTO.fromVehicle(vehicleService.updateVehicleStatus(id, vehicle));
    }
}

