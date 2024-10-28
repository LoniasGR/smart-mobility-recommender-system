package gr.iccs.smart.mobility.openrouteservice;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException.TooManyRequests;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.iccs.smart.mobility.config.OrsPropertiesConfig;
import gr.iccs.smart.mobility.geojson.FeatureCollection;

@Component
public class Directions extends Base {
    private static final List<String> validProfiles = List.of("driving-car",
            "driving-hvg", "cycling-regular", "cycling-road", "cycling-mountain",
            "cycling-electric", "foot-walking", "foot-hiking", "wheelchair");

    private static final List<LocalDateTime> rate = new LinkedList<>();

    private static final Logger log = LoggerFactory.getLogger(Directions.class);

    public Directions(
            OrsPropertiesConfig config) {
        super(config);
        baseURL += "/directions";
    }

    private Long timeToWaitForRateLimit() {
        if (rateLimit == 0) {
            return 0L;
        }
        if (rate.size() < rateLimit) {
            return 0L;
        }
        var duration = Duration.between(rate.getFirst(), LocalDateTime.now()).toMillis();
        log.info("Duration is: {}", duration);
        if (duration > 50000L) {
            return 0L;
        }
        // We use a little bit more delay just to be sure
        return 66000L - duration;
    }

    private void updateRateLimit() {
        if (rateLimit == 0) {
            return;
        }

        if (rate.size() == rateLimit) {
            rate.removeFirst();
        }
        rate.addLast(LocalDateTime.now());
    }

    private void waitToRuntimeException(Long waitMillis) {
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public FeatureCollection getDirectionsService(String profile, Point start, Point end) {
        if (!verifyProfile(profile)) {
            throw new IllegalArgumentException("The profile needs to be one of " + validProfiles.toString());
        }
        if (rate.size() > 2) {
            log.info("First element time is: {}, Last element time is: {}, size is: {}", rate.getFirst(),
                    rate.getLast(), rate.size());
        }
        var waitMillis = timeToWaitForRateLimit();
        if (waitMillis > 0L) {
            log.info("Waiting for rate limit for " + waitMillis + "ms");
            waitToRuntimeException(waitMillis);
        }
        URI uri = UriComponentsBuilder
                .fromUriString(baseURL + "/" + profile)
                .queryParam("start", start.y() + "," + start.x())
                .queryParam("end", end.y() + "," + end.x())
                .encode()
                .build()
                .toUri();

        ResponseEntity<FeatureCollection> entity;
        try {
            entity = client.get().uri(uri)
                    .retrieve()
                    .toEntity(FeatureCollection.class);
        } catch (TooManyRequests e) {
            log.error("Too many requests reached... Waiting for 30s");
            waitToRuntimeException(30000L);
            return getDirectionsService(profile, start, end);
        }
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
            summaryObj.setDistance(0D);
            summaryObj.setDuration(0D);
        }
        return summaryObj;
    }
}
