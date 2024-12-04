package gr.iccs.smart.mobility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("openrouteservice")
public class OrsPropertiesConfig {
    private String apiKey = "";
    private String host;
    private String apiVersion;
    private Integer rateLimit = 0;

    public Integer getRateLimit() {
        return this.rateLimit;
    }

    public void setRateLimit(Integer ratelimit) {
        if (ratelimit == null) {
            this.rateLimit = 0;
        }
        this.rateLimit = ratelimit;
    }

    public String getAPIKey() {
        return this.apiKey;
    }

    public void setAPIKey(String apikey) {
        if (apikey == null || apikey.isBlank() || apikey.isEmpty()) {
            this.apiKey = "";
            return;
        }
        this.apiKey = apikey;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAPIVersion() {
        return this.apiVersion;
    }

    public void setAPIVersion(String apiversion) {
        this.apiVersion = apiversion;
    }

}
