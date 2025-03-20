package gr.iccs.smart.mobility.recommendation;

import java.util.List;

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
    public List<FeatureCollection> suggestRoute(
            @PathVariable String username,
            @RequestBody RecommendationRouteDTO route,
            @RequestParam(required = false) Boolean wholeMap,
            @RequestParam(required = false) Boolean previewGraph) {
        var user = userService.getById(username);
        var recommendationOptions = new RecommendationOptions(wholeMap, previewGraph, route.options());

        var start = route.origin().toPoint();
        var finish = route.destination().toPoint();

        return recommendationService.recommendationV2(start, finish, user, recommendationOptions);
    }
}
