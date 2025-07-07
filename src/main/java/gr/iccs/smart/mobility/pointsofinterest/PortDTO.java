package gr.iccs.smart.mobility.pointsofinterest;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.iccs.smart.mobility.location.LocationDTO;

public record PortDTO(@JsonProperty("port_id") String id, String name, LocationDTO location) {

        public Port toPort() {
                return new Port(id(), name(), location().toPoint(), null, null);
        }
}
