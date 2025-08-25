package gr.iccs.smart.mobility.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;

public enum FeatureFlags implements Feature {

    @Label("Vehicle Clustering")
    CLUSTERING,
    @EnabledByDefault
    @Label("MQTT Integration")
    MQTT;
}
