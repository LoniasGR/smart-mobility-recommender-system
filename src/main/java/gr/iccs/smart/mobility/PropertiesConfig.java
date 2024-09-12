package gr.iccs.smart.mobility;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties("openrouteservice")
public class PropertiesConfig {
    private String apiKey;
    private String host;
    private String apiVersion;

    public String getAPIKey() {
        return apiKey;
    }

    public void setAPIKey(String APIKey) {
        this.apiKey = APIKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAPIVersion() {
        return apiVersion;
    }

    public void setAPIVersion(String APIVersion) {
        this.apiVersion = APIVersion;
    }

}
