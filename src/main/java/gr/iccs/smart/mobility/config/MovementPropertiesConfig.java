package gr.iccs.smart.mobility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("distances")
public class MovementPropertiesConfig {
    private Double maxWalkingDistance;
    private Double maxScooterDistance;

    public Double getMaxWalkingDistance() {
        return maxWalkingDistance;
    }

    public void setMaxWalkingDistance(Double maxwalkingdistance) {
        this.maxWalkingDistance = maxwalkingdistance;
    }

    public Double getMaxScooterDistance() {
        return maxScooterDistance;
    }

    public void setMaxScooterDistance(Double maxscooterdistance) {
        this.maxScooterDistance = maxscooterdistance;
    }

}
