package gr.iccs.smart.mobility.pointsOfInterest;

import java.util.List;

public class PortWrapper {
    private List<PortDTO> ports;

    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public List<PortDTO> getPorts() {
        return ports;
    }

    public void setPorts(List<PortDTO> ports) {
        this.ports = ports;
    }
}