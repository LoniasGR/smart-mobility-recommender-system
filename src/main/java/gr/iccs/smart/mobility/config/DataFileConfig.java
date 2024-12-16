package gr.iccs.smart.mobility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("dataFiles")
public class DataFileConfig {
    private String carLocations;
    private String portLocations;

    public String getPortLocations() {
        return this.portLocations;
    }

    public void setPortLocations(String portLocations) {
        this.portLocations = portLocations;
    }

    public String getCarLocations() {
        return this.carLocations;
    }

    public void setFilename(String carLocations) {
        this.carLocations = carLocations;
    }
}
