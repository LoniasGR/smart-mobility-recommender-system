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
        private Double maxCarDistance = null;

        public Double getMaxCarDistanceKms() {
            return maxCarDistance;
        }

        public Double getMaxCarDistanceMeters() {
            return maxCarDistance == null ? null : maxCarDistance * 1000;
        }

        public void setMaxCarDistance(Double maxCarDistance) {
            this.maxCarDistance = maxCarDistance;
        }

        public Double getMaxWalkingDistanceKms() {
            return maxWalkingDistance;
        }

        public Double getMaxWalkingDistanceMeters() {
            return maxWalkingDistance * 1000;
        }

        public void setMaxWalkingDistance(Double maxWalkingDistance) {
            this.maxWalkingDistance = maxWalkingDistance;
        }

        public Double getMaxScooterDistanceKms() {
            return maxScooterDistance;
        }

        public void setMaxScooterDistance(Double maxScooterDistance) {
            this.maxScooterDistance = maxScooterDistance;
        }

        public Double getMaxScooterDistanceMeters() {
            return maxScooterDistance * 1000;
        }
    }

    public static class Speeds {
        private Double boatSpeed;
        private Double maxCarSpeed;

        public Double getMaxCarSpeed() {
            return maxCarSpeed;
        }

        public void setMaxCarSpeed(Double maxCarSpeed) {
            this.maxCarSpeed = maxCarSpeed;
        }

        public Double getMaxScooterSpeed() {
            return maxScooterSpeed;
        }

        public void setMaxScooterSpeed(Double maxScooterSpeed) {
            this.maxScooterSpeed = maxScooterSpeed;
        }

        private Double maxScooterSpeed;

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
