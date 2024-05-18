package gr.iccs.smart.mobility.geojson;

import java.util.ArrayList;
import java.util.List;

public class FeatureCollection {
    private final String type = "FeatureCollection";
    private List<Feature> features;

    public FeatureCollection() {
        this.features = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

}
