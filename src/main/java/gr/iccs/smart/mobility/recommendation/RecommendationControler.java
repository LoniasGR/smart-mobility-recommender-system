package gr.iccs.smart.mobility.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.user.UserService;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationControler {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @PostMapping("/{username}")
    public FeatureCollection suggestRoute(
            @PathVariable String username,
            @RequestBody RecommendationRouteDTO route,
            @RequestParam(required = false) Boolean wholeMap) {
        var user = userService.getById(username);
        var options = new RecommendationOptions(wholeMap, route.options());

        var start = route.startingLocation().toPoint();
        var finish = route.endingLocation().toPoint();

        return recommendationService.recommendationV2(start, finish, user, options);
    }
}
