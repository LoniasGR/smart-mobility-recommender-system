package gr.iccs.smart.mobility.usage;

import gr.iccs.smart.mobility.vehicle.Vehicle;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

public record UseDTO(@Valid Vehicle vehicle,
                     @Valid UseStatus status,
                     @Valid Double latitude,
                     @Valid Double longitude,
                     @Valid LocalDateTime time) {
}
