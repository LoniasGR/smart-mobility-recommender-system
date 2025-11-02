package gr.iccs.smart.mobility.location;

import java.util.List;
import java.util.Random;

public class IstanbulLocations {
    private static final Random random = new Random();
    public static List<LocationDTO> landmarkLocations = List.of(
            new LocationDTO(41.0085, 28.9799),
            new LocationDTO(41.0115, 28.9833),
            new LocationDTO(41.0054, 28.9768),
            new LocationDTO(41.0256, 28.9744),
            new LocationDTO(41.0105, 28.9688),
            new LocationDTO(41.0365, 28.9850),
            new LocationDTO(41.0387, 28.9981),
            new LocationDTO(41.0167, 28.9603),
            new LocationDTO(41.0172, 28.9708),
            new LocationDTO(41.00501, 29.01697)
    );

    public static List<LocationDTO> coastLocations = List.of(
            new LocationDTO(41.0273, 29.0147),
            new LocationDTO(41.0180, 29.0086),
            new LocationDTO(40.9911, 29.018),
            new LocationDTO(41.0370, 29.0297),
            new LocationDTO(41.0449, 29.044),
            new LocationDTO(41.0510, 29.051),
            new LocationDTO(41.016, 28.9774),
            new LocationDTO(41.02185, 28.9764),
            new LocationDTO(41.032, 28.994),
            new LocationDTO( 41.0410, 29.007),
            new LocationDTO(41.0471, 29.0269)
    );

    public static LocationDTO locationPerturbation(LocationDTO location) {
        return new LocationDTO(
                location.latitude() + (random.nextDouble() - 1) / 10000,
                location.longitude() + (random.nextDouble() - 1) / 10000
        );
    }

    public static LocationDTO randomLandLocation() {
        var landmarkLocation = landmarkLocations.get(random.nextInt(landmarkLocations.size()));
        return locationPerturbation(landmarkLocation);
    }

    public static LocationDTO randomCoastLocation() {
        return coastLocations.get(random.nextInt(coastLocations.size()));
    }
}
