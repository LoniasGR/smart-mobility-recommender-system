package gr.iccs.smart.mobility.pointsofinterest;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.iccs.smart.mobility.location.LocationDTO;

public record BusStopDTO(@JsonProperty("bus_stop_id") String id, String name, LocationDTO location) {

        public BusStop toBusStop() {
                return new BusStop(id(), name(), location().toPoint(), null);
        }
}
