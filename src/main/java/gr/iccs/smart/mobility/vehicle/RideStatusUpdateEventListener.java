package gr.iccs.smart.mobility.vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import gr.iccs.smart.mobility.usage.RideStatusUpdateEvent;
import gr.iccs.smart.mobility.usage.UseStatus;

public class RideStatusUpdateEventListener implements ApplicationListener<RideStatusUpdateEvent> {

    @Autowired
    private VehicleService vehicleService;

    @Override
    public void onApplicationEvent(RideStatusUpdateEvent event) {
        VehicleStatus vehicleStatus;
        var v = vehicleService.getById(event.getVehicleId());
        var useInfo = event.getUseInfo();

        if (useInfo.status().equals(UseStatus.ACTIVE)) {
            vehicleStatus = VehicleStatus.IN_USE;
            // Since the vehicle is in use, we remove it from the graph
            if (v.isLandVehicle()) {
                vehicleService.deleteAllConnectionsOfLandVehicle(event.getVehicleId());
            } else {
                // TODO: Update the port, based on the existing sea vessels
            }
        } else {
            vehicleStatus = VehicleStatus.IDLE;
            // Here we should be adding the new relationships to the database
            // This needs to include incoming relationships and outgoing ones.
        }
        var vehicleInfo = new VehicleInfoDTO(null,
                useInfo.location().latitude(),
                useInfo.location().longitude(),
                vehicleStatus);

        // TODO: to check what needs to be changed here
        vehicleService.updateVehicleStatus(event.getVehicleId(), vehicleInfo);

    }
}
