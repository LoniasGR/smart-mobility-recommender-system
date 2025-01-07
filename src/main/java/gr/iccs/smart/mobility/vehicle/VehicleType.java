package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.Map;
import java.util.Random;

public enum VehicleType {
    SEA_VESSEL,
    CAR,
    SCOOTER;

    private static final List<VehicleType> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();
    private static final Map<VehicleType, String> nodeMapping = Map.of(VehicleType.SEA_VESSEL, "Sea Vessel",
            VehicleType.CAR, "Car",
            VehicleType.SCOOTER, "Scooter");

    public static VehicleType randomVehicleType() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

    public static String nodeOf(VehicleType t) {
        return nodeMapping.get(t);
    }
}
