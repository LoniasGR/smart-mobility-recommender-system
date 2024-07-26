package gr.iccs.smart.mobility.openrouteservice;

import gr.iccs.smart.mobility.PropertiesConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Base {
    protected RestClient client;
    protected String APIKey;
    protected String baseURL;

    public Base(PropertiesConfig config) {
        this.APIKey = config.getAPIKey();
        this.baseURL = config.getHost() + "/" + config.getAPIVersion();

        this.client = RestClient.builder()
                .defaultHeader("Authorization", this.APIKey)
                .defaultHeader("Accept","application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                .build();
    }
}
