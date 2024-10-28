package gr.iccs.smart.mobility.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.user.UserRouteDTO;
import gr.iccs.smart.mobility.user.UserService;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationControler {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @PostMapping("/{username}")
    public void suggestRoute(@PathVariable String username, @RequestBody UserRouteDTO route) {
        var user = userService.getById(username);
        var start = route.startingLocation().toPoint();
        var finish = route.endingLocation().toPoint();
        recommendationService.recommendationV2(start, finish, user);
    }
}
