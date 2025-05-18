package gr.iccs.smart.mobility.usage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class RideStatusUpdateEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    private static final Logger log = LoggerFactory.getLogger(RideStatusUpdateEventPublisher.class);

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishRideStatusUpdateEvent(UseDTO useInfo, String vehicleId) {
        log.debug("Publishing ride status update event: {} for vehicleId: {}", useInfo.status(), vehicleId);
        var event = new RideStatusUpdateEvent(this, useInfo, vehicleId);
        publisher.publishEvent(event);
    }
}
