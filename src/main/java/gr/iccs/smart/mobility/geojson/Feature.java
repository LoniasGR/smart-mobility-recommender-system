package gr.iccs.smart.mobility.geojson;

import java.util.HashMap;

public class Feature {
    private final String type = "Feature";

    private Geometry geometry;

    private HashMap<String, String> properties;

    public Feature() {
        this.geometry = new Geometry();
        this.properties = new HashMap<>();
    }

    public String getType() {
        return type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
}
