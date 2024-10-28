package gr.iccs.smart.mobility.pointsOfInterest;

import gr.iccs.smart.mobility.location.LocationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/boat-stop")
public class BoatStopController {
    private static final Logger log = LoggerFactory.getLogger(BoatStopController.class);

    @Autowired
    private BoatStopService boatStopService;

    @GetMapping
    public List<BoatStop> getAll() {
        log.debug("BoatStop API: Get All");
        return boatStopService.getAll();
    }

    @PostMapping("/locate")
    public List<BoatStop> getByLocationNear(@RequestBody LocationDTO location) {
        log.debug("BoatStop API: getByLocationNear");
        return boatStopService.getByLocationNear(location.toPoint());
    }

    @PostMapping("/exact-location")
    public BoatStop getByLocationExact(@RequestBody LocationDTO location) {
        log.debug("BoatStop API: getByLocationExact");
        var boatStop = boatStopService.getByExactLocation(location.toPoint());
        if (boatStop.isEmpty()) {
            throw new InvalidBoatStopException("The boat stop does not exist.");
        }
        return boatStop.get();
    }

    @GetMapping("/{id}")
    public BoatStop getById(@PathVariable String id) {
        log.debug("BoatStop API:Get by id {}", id);
        var boatStop = boatStopService.getByID(UUID.fromString(id));
        if (boatStop.isEmpty()) {
            throw new InvalidBoatStopException("The boat stop does not exist.");
        }
        return boatStop.get();
    }

    @PostMapping
    public BoatStop create(@RequestBody BoatStop boatStop) {
        log.debug("BoatStop API: create");
        return boatStopService.create(boatStop);
    }

}