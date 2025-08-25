package gr.iccs.smart.mobility.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.stereotype.Service;
import org.togglz.core.manager.FeatureManager;

import gr.iccs.smart.mobility.config.FeatureFlags;
import gr.iccs.smart.mobility.config.MqttConfig;
import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.vehicle.VehicleService;

@Service
public class MqttListener {

    private static final Logger log = LoggerFactory.getLogger(MqttListener.class);

    private final VehicleService vehicleService;
    private final MqttConfig conf;
    private final FeatureManager togglzFeatureManager;

    public MqttListener(VehicleService vehicleService, MqttConfig mqttConfig, FeatureManager togglzFeatureManager) {
        this.vehicleService = vehicleService;
        this.conf = mqttConfig;
        this.togglzFeatureManager = togglzFeatureManager;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        var factory = new DefaultMqttPahoClientFactory();
        var options = new MqttConnectOptions();
        options.setServerURIs(new String[] { conf.getUrl() });
        if (conf.getUsername() != null && !conf.getUsername().isEmpty() &&
                conf.getPassword() != null && !conf.getPassword().isEmpty()) {
            options.setUserName(conf.getUsername());
            options.setPassword(conf.getPassword().toCharArray());

        }
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound(MqttPahoClientFactory mqttClientFactory) {
        var adapter = new MqttPahoMessageDrivenChannelAdapter(conf.getClientId(), mqttClientFactory, conf.getTopics());
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }

    @Bean
    public IntegrationFlow mqttInFlow(MessageProducerSupport mqttInbound) {
        return IntegrationFlow.from(mqttInbound)
                .transform(new JsonToObjectTransformer(VehicleStatus.class))
                .handle(m -> {
                    try {
                        processVehicleStatus((VehicleStatus) m.getPayload());
                    } catch (Exception ex) {
                        // Custom error handling logic, e.g. log, route, etc.
                        log.error("Failed to process VehicleStatus", ex);
                    }
                })
                .get();
    }

    public void processVehicleStatus(VehicleStatus vehicleStatus) {
        if (togglzFeatureManager.isActive(FeatureFlags.MQTT)) {
            log.info("Received vehicle status: {}", vehicleStatus);
            vehicleService.updateVehicleLocation("test-1",
                    new LocationDTO(vehicleStatus.latitude(), vehicleStatus.longitude()), true);
        } else {
            log.debug("MQTT is disabled, skipping VehicleStatus processing.");
        }
    }
}
