package gr.iccs.smart.mobility.usage;

import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.vehicle.Vehicle;

@Service
public class UsageService {

    public Used createOrUpdateRide(Used rideInfo, UseDTO useInfo, Vehicle vehicle) {
        var location = useInfo.location().toPoint();
        var ride = rideInfo;
        if (ride == null) {
            ride = new Used();
            ride.setVehicle(vehicle);
            ride.setStartingLocation(location);
            ride.setStartingTime(useInfo.time());
        } else {
            ride.setEndingLocation(location);
            ride.setEndingTime(useInfo.time());
        }
        ride.setStatus(useInfo.status());
        return ride;
    }
}
