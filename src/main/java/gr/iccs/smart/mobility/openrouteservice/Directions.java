package gr.iccs.smart.mobility.openrouteservice;

import gr.iccs.smart.mobility.PropertiesConfig;
import gr.iccs.smart.mobility.geojson.FeatureCollection;
import org.neo4j.driver.types.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
public class Directions extends Base {
    private static final List<String> validProfiles = List.of("driving-car",
            "driving-hvg", "cycling-regular", "cycling-road", "cycling-mountain",
            "cycling-electric", "foot-walking", "foot-hiking", "wheelchair");

    public Directions(PropertiesConfig config) {
        super(config);
        this.baseURL += "/directions";
    }

    public FeatureCollection getDirectionsService(String profile, Point start, Point end) {
        if(!verifyProfile(profile)) {
            throw new IllegalArgumentException("The profile needs to be one of " + validProfiles.toString());
        }
        URI uri = UriComponentsBuilder
                .fromUriString(baseURL + "/" + profile)
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

    public boolean verifyProfile(String profile) {
        return validProfiles.contains(profile);
    }
}
