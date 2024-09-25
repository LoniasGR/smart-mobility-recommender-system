package gr.iccs.smart.mobility.openrouteservice;

import gr.iccs.smart.mobility.PropertiesConfig;
import gr.iccs.smart.mobility.geojson.FeatureCollection;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Component
public class Directions extends Base {
    private static final List<String> validProfiles = List.of("driving-car",
            "driving-hvg", "cycling-regular", "cycling-road", "cycling-mountain",
            "cycling-electric", "foot-walking", "foot-hiking", "wheelchair");

    private static final List<LocalDateTime> rate = new LinkedList<>();

    private static final Logger log = LoggerFactory.getLogger(Directions.class);

    public Directions(
            PropertiesConfig config) {
        super(config);
        baseURL += "/directions";
    }

    private Long timeToWaitForRateLimit() {
        if (rate.size() < rateLimit) {
            return 0L;
        }
        var duration = Duration.between(rate.getFirst(), LocalDateTime.now()).toMillis();
        if (duration > 60000L) {
            return 0L;
        }
        // We use a little bit more delay just to be sure
        return 62000L - duration;
    }

    private void updateRateLimit() {
        if (rate.size() == rateLimit) {
            rate.removeLast();
        }
        rate.addLast(LocalDateTime.now());
    }

    public FeatureCollection getDirectionsService(String profile, Point start, Point end) {
        if (!verifyProfile(profile)) {
            throw new IllegalArgumentException("The profile needs to be one of " + validProfiles.toString());
        }
        var waitMillis = timeToWaitForRateLimit();
        if (waitMillis > 0L) {
            log.info("Waiting for rate limit for " + waitMillis + "ms");
            try {
                Thread.sleep(waitMillis);
            } catch (Exception e) {
                log.error("Failed to sleep for rate limiting", e);
            }
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

        updateRateLimit();
        return entity.getBody();
    }

    public boolean verifyProfile(String profile) {
        return validProfiles.contains(profile);
    }

    public Summary getDirectionsSummary(String profile, Point start, Point end) {
        var directions = this.getDirectionsService(profile, start, end);
        var summary = directions.getFeatures().getFirst().getProperties().get("summary");
        ObjectMapper mapper = new ObjectMapper();
        var summaryObj = mapper.convertValue(summary, Summary.class);
        if (summaryObj.getDistance() == null || summaryObj.getDuration() == null) {
            summaryObj.setDistance(0F);
            summaryObj.setDuration(0F);
        }
        return summaryObj;
    }
}
