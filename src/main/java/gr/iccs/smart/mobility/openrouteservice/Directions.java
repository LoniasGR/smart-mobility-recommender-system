package gr.iccs.smart.mobility.openrouteservice;

import gr.iccs.smart.mobility.PropertiesConfig;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import org.neo4j.driver.types.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class Directions extends Base {

    public Directions(PropertiesConfig config) {
        super(config);
        this.baseURL += "/directions";
    }

    public FeatureCollection getDirectionsService(Point start, Point end) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseURL + "/driving-car")
                .queryParam("start", start.y() + "," + start.x())
                .queryParam("end", end.y() + "," + end.x())
                .encode()
                .build()
                .toUri();

        ResponseEntity<FeatureCollection> entity = client.get().uri(uri)
                .retrieve()
                .toEntity(FeatureCollection.class);

        return entity.getBody();
    }
}
