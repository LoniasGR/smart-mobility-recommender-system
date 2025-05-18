package gr.iccs.smart.mobility.openrouteservice;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
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
        log.debug("Duration is: {}", duration);
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

    private void verifyProfile(String profile) {
        if (!validProfiles.contains(profile)) {
            throw new IllegalArgumentException("The profile needs to be one of " + validProfiles.toString());
        }
    }

    private String verifyReturnType(String returnType) {
        List<String> returnTypes = List.of("json", "geojson", "gpx", "");
        if (returnType != null && !returnTypes.contains(returnType)) {
            throw new IllegalArgumentException("The returnType needs to be one of " + validProfiles.toString());
        }

        if (returnType == null || "".equals(returnType)) {
            return "";
        }
        return "/" + returnType;

    }

    private Class<?> extrapolateType(String returnType) {
        // TODO: Add the rest of mappings
        final Map<String, Class<?>> returnTypeMapping = Map.of(
                "geojson", FeatureCollection.class,
                "", FeatureCollection.class);

        if (returnType == null) {
            return FeatureCollection.class;
        }
        var returnClass = returnTypeMapping.get(returnType);
        if (returnClass == null) {
            throw new IllegalArgumentException("This return type is not implemented yet");
        }
        return returnClass;
    }

    /**
     * Rate limit function wrapper
     * 
     * @param callback
     */
    private <T> T executeWithRateLimiting(Supplier<T> callback) {
        if (rate.size() > 2) {
            log.warn("First element time is: {}, Last element time is: {}, size is: {}", rate.getFirst(),
                    rate.getLast(), rate.size());
        }
        var waitMillis = timeToWaitForRateLimit();
        if (waitMillis > 0L) {
            log.warn("Waiting for rate limit for {}ms", waitMillis);
            waitToRuntimeException(waitMillis);
        }
        T ret;
        try {
            ret = callback.get();
        } catch (TooManyRequests e) {
            log.error("Too many requests reached... Waiting for 30s");
            waitToRuntimeException(30000L);
            ret = executeWithRateLimiting(callback);
        }
        updateRateLimit();
        return ret;
    }

    public FeatureCollection getDirectionsService(String profile, Point start, Point end) {
        verifyProfile(profile);

        return executeWithRateLimiting(() -> {
            URI uri = UriComponentsBuilder
                    .fromUriString(baseURL + "/" + profile)
                    .queryParam("start", start.y() + "," + start.x())
                    .queryParam("end", end.y() + "," + end.x())
                    .encode()
                    .build()
                    .toUri();

            log.debug("Calling URL: {}", uri);
            ResponseEntity<FeatureCollection> entity = client.get().uri(uri)
                    .retrieve()
                    .toEntity(FeatureCollection.class);

            return entity.getBody();
        });
    }

    public <T> T postDirectionsService(String profile, DirectionsOptions options, String returnType) {
        verifyProfile(profile);
        var format = verifyReturnType(returnType);
        Class<T> clazz = (Class<T>) extrapolateType(returnType);

        return executeWithRateLimiting(() -> {
            URI uri = UriComponentsBuilder
                    .fromUriString(baseURL + "/" + profile + format)
                    .encode()
                    .build()
                    .toUri();
            log.debug("Calling URL: {}", uri);

            ResponseEntity<T> entity = client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(options)
                    .retrieve()
                    .toEntity(clazz);
            return entity.getBody();
        });
    }

    public Summary getDirectionsSummary(String profile, Point start, Point end) {
        var options = new DirectionsOptions(List.of(List.of(start.y(), start.x()), List.of(end.y(), end.x())));
        FeatureCollection directions = this.postDirectionsService(profile, options, "geojson");
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
