package gr.iccs.smart.mobility.recommendation;

import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.vehicle.Vehicle;
import gr.iccs.smart.mobility.vehicle.VehicleService;
import org.neo4j.driver.types.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {
    private final UserService userService;
    private final VehicleService vehicleService;

    public RecommendationService(UserService userService, VehicleService vehicleService) {
        this.userService = userService;
        this.vehicleService = vehicleService;
    }

    public List<Vehicle> recommend(Point startingPoint, Point finishingPoint) {
        Distance distance = new Distance(1, Metrics.KILOMETERS);
        return vehicleService.findNearLocation(startingPoint, distance);
    }
}
