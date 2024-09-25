package gr.iccs.smart.mobility;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties("openrouteservice")
public class PropertiesConfig {
    private String apiKey;
    private String host;
    private String apiVersion;
    private Integer rateLimit;

    public Integer getRateLimit() {
        return this.rateLimit;
    }

    public void setRateLimit(Integer ratelimit) {
        this.rateLimit = ratelimit;
    }

    public String getAPIKey() {
        return this.apiKey;
    }

    public void setAPIKey(String apikey) {
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
