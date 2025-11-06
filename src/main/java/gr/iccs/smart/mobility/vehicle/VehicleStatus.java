package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.Random;

public enum VehicleStatus {
    CREATING,
    IN_USE,
    RESERVED,
    IDLE,
    CHARGING,
    UNAVAILABLE;

    private static final List<VehicleStatus> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static VehicleStatus randomVehicleStatus() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
