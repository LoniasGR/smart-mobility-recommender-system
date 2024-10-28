package gr.iccs.smart.mobility.graph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    @Autowired
    private GraphService graphService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public void preCreateGraph() {
        graphService.graphPreCalculation();
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete")
    public void deleteGraph() {
        graphService.graphDestruction();
    }
}
