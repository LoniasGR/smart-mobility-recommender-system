package gr.iccs.smart.mobility.geojson;

import java.util.ArrayList;
import java.util.List;

public class Geometry {
    private final String type = "Point";
    private List<Double> coordinates = new ArrayList<>();

    public String getType() {
        return type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

}
