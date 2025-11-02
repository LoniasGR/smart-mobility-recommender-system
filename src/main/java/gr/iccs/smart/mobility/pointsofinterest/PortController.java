package gr.iccs.smart.mobility.pointsofinterest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.location.LocationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/ports")
@Tag(name = "Ports", description = "Retrieve and manage ports")
public class PortController {
    private static final Logger log = LoggerFactory.getLogger(PortController.class);

    private final PointOfInterestService portService;

    PortController(PointOfInterestService pointOfInterestService) {
        this.portService = pointOfInterestService;
    }

    @GetMapping
    @Operation(summary = "Get all ports", description = "Retrieve a list of all ports", tags = { "Ports" })
    public List<Port> getAll() {
        return portService.getAllPorts();
    }

    @PostMapping("/locate")
    @Operation(summary = "Get port by approximate location", description = "Retrieve a port nearby a specific location", tags = { "Ports" })
    public List<Port> getByLocationNear(@RequestBody LocationDTO location) {
        log.debug("Port API: getByLocationNear");
        return portService.getByLocationNear(location.toPoint());
    }

    @PostMapping("/exact-location")
    @Operation(summary = "Get port by location", description = "Retrieve a port by specific location", tags = { "Ports" })
    public Port getByLocationExact(@RequestBody LocationDTO location) {
        log.debug("Port API: getByLocationExact");
        var port = portService.getByExactLocation(location.toPoint());
        if (port.isEmpty()) {
            throw new InvalidPortException("The boat stop does not exist.");
        }
        return port.get();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get port by id", description = "Retrieve a port from the id", tags = { "Ports" })
    public Port getById(@PathVariable String id) {
        log.debug("Port API:Get by id {}", id);
        var port = portService.getByID(id);
        if (port.isEmpty()) {
            throw new InvalidPortException("The boat stop does not exist.");
        }
        return (Port) port.get();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create port", description = "Create a port", tags = { "Ports" })
    public Port create(@RequestBody PortDTO port) {
        log.info("Creating port {}", port);
        return (Port) portService.create(port.toPort());
    }

}