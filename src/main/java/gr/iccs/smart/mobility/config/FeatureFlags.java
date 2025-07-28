package gr.iccs.smart.mobility.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum FeatureFlags implements Feature {

    @Label("Vehicle Clustering")
    CLUSTERING,
    @Label("MQTT Integration")
    MQTT;
    
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
