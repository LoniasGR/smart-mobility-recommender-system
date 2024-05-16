package gr.iccs.smart.mobility.boatStop;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.location.LocationDTO;

@RestController
@RequestMapping("api/boat-stop")
public class BoatStopController {
    private static final Logger log = LoggerFactory.getLogger(BoatStopController.class);
    
    private final BoatStopService boatStopService;

    public BoatStopController(BoatStopService boatStopService) {
        this.boatStopService = boatStopService;
    }

    @GetMapping
    public List<BoatStop> getAll() {
        log.debug("BoatStop API: Get All");
        return boatStopService.getAll();
    }

    @PostMapping("/locate")
    public List<BoatStop> getByLocationNear(@RequestBody LocationDTO location) {
        log.debug("BoatStop API: getByLocation");
        return boatStopService.getByLocationNear(location.toPoint());
    }

    @PostMapping
    public BoatStop create(@RequestBody BoatStop boatStop) {
        log.debug("BoatStop API: create");
        return boatStopService.create(boatStop);
    }

}