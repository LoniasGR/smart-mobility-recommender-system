package gr.iccs.smart.mobility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("transportation")
public class TransportationPropertiesConfig {
    private Speeds speeds = new Speeds();
    private Distances distances = new Distances();

    public Speeds getSpeeds() {
        return speeds;
    }

    public void setSpeeds(Speeds speeds) {
        this.speeds = speeds;
    }

    public Distances getDistances() {
        return distances;
    }

    public void setDistances(Distances distances) {
        this.distances = distances;
    }

    public static class Distances {
        private Double maxWalkingDistance = 2.0;
        private Double maxScooterDistance = 5.0;

        public Double getMaxWalkingDistance() {
            return maxWalkingDistance;
        }

        public Double getMaxWalkingDistanceKilometers() {
            return maxWalkingDistance * 1000;
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

        public Double getMaxScooterDistanceKilometers() {
            return maxScooterDistance * 1000;
        }
    }

    public static class Speeds {
        private Double boatSpeed;

        public Double getBoatSpeed() {
            return boatSpeed;
        }

        public Double getBoatSpeedMetersPerSecond() {
            return boatSpeed * 0.51444;
        }

        public void setBoatSpeed(Double boatSpeed) {
            this.boatSpeed = boatSpeed;
        }
    }
}
