package gr.iccs.smart.mobility.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;

@Configuration
public class TogglzConfiguration {

    @Bean("featureProvider")
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(FeatureFlags.class);
    }
}