package gr.iccs.smart.mobility.pointsOfInterest;

import gr.iccs.smart.mobility.location.IstanbulLocations;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import org.neo4j.driver.types.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BoatStopService {

    private final BoatStopRepository boatStopRepository;

    public BoatStopService(BoatStopRepository boatStopRepository) {
        this.boatStopRepository = boatStopRepository;
    }

    public List<BoatStop> getAll() {
        return boatStopRepository.findAll();
    }

    public Optional<BoatStop> getByID(UUID id) {
        return boatStopRepository.findById(id);
    }

    public List<BoatStop> getByLocationNear(Point location) {
        return boatStopRepository.findByLocationNear(location);
    }

    public Optional<BoatStop> getByExactLocation(Point location) {
        return boatStopRepository.findByLocation(location);
    }
    public BoatStop create(BoatStop boatStop) {
        var locationExists = boatStopRepository.findByLocation(boatStop.getLocation());

        if (locationExists.isPresent()) {
            throw new IllegalArgumentException("There is already a boat stop at the specified location");
        }
        return boatStopRepository.save(boatStop);
    }

    public BoatStop update(BoatStop newBoatStop) {
        var oldBoatStop = boatStopRepository.findById(newBoatStop.getId());
        if(oldBoatStop.isEmpty()) {
            throw new IllegalArgumentException("There is no boat stop to update");
        }
        return boatStopRepository.save(newBoatStop);
    }

    public void removeVehicle(BoatStop boatStop, Vehicle v) {
        boatStopRepository.deleteParkedIn(boatStop.getId(), v.getId());
    }

    public void createBoatStopScenario() {
        for (int i = 0; i < IstanbulLocations.coastLocations.size(); i++) {
            var boardStop = new BoatStop(UUID.randomUUID(), IstanbulLocations.coastLocations.get(i).toPoint());
            create(boardStop);
        }
    }

}
