package gr.iccs.smart.mobility.openrouteservice;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import gr.iccs.smart.mobility.config.OrsPropertiesConfig;

@Component
public class Base {
    protected static RestClient client;
    protected static Integer rateLimit;
    protected static String apiKey;
    protected String baseURL;

    private RestClient.Builder addApiKey(RestClient.Builder client) {
        if (!apiKey.isEmpty()) {
            return client.defaultHeader("Authorization", apiKey);

        }
        return client;
    }

    public Base(OrsPropertiesConfig config) {
        apiKey = config.getAPIKey();
        baseURL = config.getHost() + "/" + config.getAPIVersion();
        rateLimit = config.getRateLimit();

        var clientBuilder = RestClient.builder()
                .defaultHeader("Accept",
                        "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
        client = addApiKey(clientBuilder).build();
    }
}
