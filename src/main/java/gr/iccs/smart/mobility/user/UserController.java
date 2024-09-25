package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.openrouteservice.Directions;
import gr.iccs.smart.mobility.recommendation.RecommendationDTO;
import gr.iccs.smart.mobility.recommendation.RecommendationService;
import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.util.EmptyJsonResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/people")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final RecommendationService recommendationService;
    private final Directions directions;

    public UserController(UserService userService, RecommendationService recommendationService, Directions directions) {
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.directions = directions;
    }

    @GetMapping
    public List<UserDTO> getAll() {
        log.debug("User API: Get All");
        return userService.getAll()
                .stream().map(UserDTO::fromUser)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{username}")
    public UserDTO getById(@PathVariable String username) {
        log.debug("User API: Get By ID");
        return UserDTO.fromUser(userService.getById(username));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.debug("User API: Create {}", user);
        return userService.create(user);
    }

    @PostMapping("{username}/ride")
    public void rideManagement(@PathVariable String username, @RequestBody UseDTO useInfo) {
        log.debug("User API: add ride for user {}with ride data {}", username, useInfo);
        userService.manageRide(username, useInfo);
    }

    @GetMapping("{username}/ride-status")
    public ResponseEntity<?> rideStatus(@PathVariable String username) {
        log.debug("User API: get ride status for {}", username);
        var status = userService.rideStatus(username);
        if (status.isEmpty()) {
            return new ResponseEntity<>(new EmptyJsonResponse(), HttpStatus.OK);
        }
        return new ResponseEntity<>(status.get(), HttpStatus.OK);
    }

    @PostMapping("{username}/recommend")
    public List<List<RecommendationDTO>> suggestRoute(@PathVariable String username, @RequestBody UserRouteDTO route) {
        log.debug("User API: suggest route for user{} with data {}", username, route);
        var start = route.startingLocation().toPoint();
        var finish = route.endingLocation().toPoint();
        return recommendationService.recommend(start, finish);
    }

    @PostMapping("{username}/recommendation/visualize")
    public FeatureCollection visualizeRecommendation(@PathVariable String username, @RequestBody UserRouteDTO route) {
        log.debug("User API: visualize suggested route for user{} with data {}", username, route);
        var start = route.startingLocation().toPoint();
        var finish = route.endingLocation().toPoint();
        return recommendationService.createRecommendationGeoJSON(start, finish);
    }
}
