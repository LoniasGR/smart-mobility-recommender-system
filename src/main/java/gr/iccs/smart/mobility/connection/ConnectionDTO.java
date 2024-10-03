package gr.iccs.smart.mobility.connection;

import gr.iccs.smart.mobility.vehicle.LandVehicleDTO;

public interface ConnectionDTO {
    String getId();

    LandVehicleDTO getTarget();
}
