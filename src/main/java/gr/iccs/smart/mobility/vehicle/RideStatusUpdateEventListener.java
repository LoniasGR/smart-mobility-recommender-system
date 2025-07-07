package gr.iccs.smart.mobility.vehicle;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import gr.iccs.smart.mobility.usage.RideStatusUpdateEvent;
import gr.iccs.smart.mobility.usage.UseStatus;

@Component
public class RideStatusUpdateEventListener implements ApplicationListener<RideStatusUpdateEvent> {

    private final VehicleService vehicleService;
    private final VehicleGraphService vehicleGraphService;

    public RideStatusUpdateEventListener(VehicleService vehicleService,
            VehicleGraphService vehicleGraphService) {
        this.vehicleService = vehicleService;
        this.vehicleGraphService = vehicleGraphService;
    }

    @Override
    public void onApplicationEvent(RideStatusUpdateEvent event) {
        var useInfo = event.getUseInfo();
        VehicleStatus vehicleStatus;

        // We first need to do a pass on the vehicle status
        if (useInfo.status().equals(UseStatus.ACTIVE)) {
            vehicleStatus = VehicleStatus.IN_USE;
        } else {
            vehicleStatus = VehicleStatus.IDLE;
        }

        var vehicleInfo = new VehicleInfoDTO(null,
                useInfo.location().latitude(),
                useInfo.location().longitude(),
                vehicleStatus);

        vehicleService.updateVehicleStatus(event.getVehicleId(), vehicleInfo);

        var v = vehicleService.getByIdNoConnections(event.getVehicleId());

        // If the vehicle is in use, we have to remove it from the graph
        if (useInfo.status().equals(UseStatus.ACTIVE)) {
            vehicleGraphService.removeVehicleFromGraph(v);
        } else {
            // Here we should be adding the new relationships to the database
            // This needs to include incoming relationships and outgoing ones.
            vehicleGraphService.addVehicleToGraph(v);
        }
    }
}
