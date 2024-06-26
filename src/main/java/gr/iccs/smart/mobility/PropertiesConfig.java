package gr.iccs.smart.mobility;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties("openrouteservice") //part of our property configured name - prefix
public class PropertiesConfig {
    private String apikey;
    private String host;
    private String apiversion;

    public String getAPIKey() {
        return apikey;
    }

    public void setAPIKey(String APIKey) {
        this.apikey = APIKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAPIVersion() {
        return apiversion;
    }

    public void setAPIVersion(String APIVersion) {
        this.apiversion = APIVersion;
    }

}
