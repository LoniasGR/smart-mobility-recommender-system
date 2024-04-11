package gr.iccs.smart.mobility.vehicle;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping(value = {"", "/"})
    public Iterable<Vehicle> getAll() {
        return vehicleService.getAll();
    }

    @GetMapping(value = "/{id}")
    public Vehicle getById(@PathVariable UUID id) {
        return vehicleService.getById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@Valid @RequestBody Vehicle vehicle) {
        vehicleService.create(vehicle);
    }

    @PutMapping(value = "/{id}")
    public void updateStatus(@PathVariable UUID id, @RequestBody VehicleRecord vehicle) {
        vehicleService.updateVehicleStatus(id, vehicle);
    }
}

