package gr.iccs.smart.mobility.vehicle;

import java.util.List;
import java.util.Random;

public enum VehicleType {
    SEA_VESSEL,
    CAR,
    SCOOTER;

    private static final List<VehicleType> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static VehicleType randomVehicleType()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
