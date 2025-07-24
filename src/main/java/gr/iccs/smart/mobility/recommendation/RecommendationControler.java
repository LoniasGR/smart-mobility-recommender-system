package gr.iccs.smart.mobility.recommendation;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.user.UserService;
import gr.iccs.smart.mobility.util.FormatSelection;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationControler {

    private final RecommendationService recommendationService;
    private final UserService userService;

    RecommendationControler(RecommendationService recommendationService,
            UserService userService) {
        this.recommendationService = recommendationService;
        this.userService = userService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<RecommendationResponse> suggestRoute(
            @PathVariable String username,
            @RequestBody RecommendationRouteDTO route,
            @RequestParam(required = false, defaultValue = "false") Boolean wholeMap,
            @RequestParam(required = false, defaultValue = "false") Boolean previewGraph,
            @RequestParam(required = false, defaultValue = "json") FormatSelection format) {
        var user = userService.getById(username);
        var recommendationOptions = new RecommendationOptions(wholeMap, previewGraph, route.options());

        var start = route.origin().toPoint();
        var finish = route.destination().toPoint();
        if (format == FormatSelection.GEOJSON) {
            var res = recommendationService.geojsonRecommendation(start, finish, user, recommendationOptions);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/geo+json"))
                    .body(new GeoJsonResponse(res));
        } else {
            var res = recommendationService.jsonRecommendation(start, finish, user, recommendationOptions);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new RecommendationJsonResponse(res));
        }
    }
}
