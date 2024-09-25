package gr.iccs.smart.mobility.openrouteservice;

import gr.iccs.smart.mobility.PropertiesConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Base {
    protected static RestClient client;
    protected static Integer rateLimit;
    protected static String APIKey;
    protected String baseURL;

    public Base(PropertiesConfig config) {
        APIKey = config.getAPIKey();
        baseURL = config.getHost() + "/" + config.getAPIVersion();
        rateLimit = config.getRateLimit();

        client = RestClient.builder()
                .defaultHeader("Authorization", APIKey)
                .defaultHeader("Accept",
                        "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                .build();
    }
}
