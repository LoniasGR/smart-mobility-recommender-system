package gr.iccs.smart.mobility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("distances")
public class MovementPropertiesConfig {
    private Long maxWalkingDistance;
    private Long maxScooterDistance;

    public Long getMaxWalkingDistance() {
        return maxWalkingDistance;
    }

    public void setMaxWalkingDistance(Long maxwalkingdistance) {
        this.maxWalkingDistance = maxwalkingdistance;
    }

    public Long getMaxScooterDistance() {
        return maxScooterDistance;
    }

    public void setMaxScooterDistance(Long maxscooterdistance) {
        this.maxScooterDistance = maxscooterdistance;
    }

}
