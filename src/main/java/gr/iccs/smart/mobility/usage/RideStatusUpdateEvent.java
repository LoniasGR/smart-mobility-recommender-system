package gr.iccs.smart.mobility.usage;

import org.springframework.context.ApplicationEvent;

public class RideStatusUpdateEvent extends ApplicationEvent {
    private UseDTO useInfo;
    private String vehicleId;

    public RideStatusUpdateEvent(Object source, UseDTO useInfo, String vehicleId) {
        super(source);
        this.useInfo = useInfo;
        this.vehicleId = vehicleId;
    }

    public UseDTO getUseInfo() {
        return useInfo;
    }

    public String getVehicleId() {
        return vehicleId;
    }
}
