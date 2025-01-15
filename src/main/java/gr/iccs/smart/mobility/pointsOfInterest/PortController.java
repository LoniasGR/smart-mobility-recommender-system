package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.location.LocationDTO;

@RestController
@RequestMapping("api/boat-stop")
public class PortController {
    private static final Logger log = LoggerFactory.getLogger(PortController.class);

    @Autowired
    private PointOfInterestService portService;

    @GetMapping
    public List<Port> getAll() {
        log.debug("Port API: Get All");
        return portService.getAllPorts();
    }

    @PostMapping("/locate")
    public List<Port> getByLocationNear(@RequestBody LocationDTO location) {
        log.debug("Port API: getByLocationNear");
        return portService.getByLocationNear(location.toPoint());
    }

    @PostMapping("/exact-location")
    public Port getByLocationExact(@RequestBody LocationDTO location) {
        log.debug("Port API: getByLocationExact");
        var port = portService.getByExactLocation(location.toPoint());
        if (port.isEmpty()) {
            throw new InvalidPortException("The boat stop does not exist.");
        }
        return port.get();
    }

    @GetMapping("/{id}")
    public Port getById(@PathVariable String id) {
        log.debug("Port API:Get by id {}", id);
        var port = portService.getByID(id);
        if (port.isEmpty()) {
            throw new InvalidPortException("The boat stop does not exist.");
        }
        return (Port) port.get();
    }

    @PostMapping
    public Port create(@RequestBody Port port) {
        log.debug("Port API: create");
        return (Port) portService.create(port);
    }

}