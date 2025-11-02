package gr.iccs.smart.mobility.config;

import static org.springdoc.core.utils.Constants.ALL_PATTERN;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
	@Profile("!prod")
	public GroupedOpenApi actuatorApi(OpenApiCustomizer actuatorOpenApiCustomizer,
			OperationCustomizer actuatorCustomizer,
			WebEndpointProperties endpointProperties,
			@Value("${springdoc.version}") String appVersion) {
		return GroupedOpenApi.builder()
				.group("Actuator")
				.pathsToMatch(endpointProperties.getBasePath() + ALL_PATTERN)
				.addOpenApiCustomizer(actuatorOpenApiCustomizer)
				.addOpenApiCustomizer(openApi -> openApi.info(new Info().title("Actuator API").version(appVersion)))
				.addOperationCustomizer(actuatorCustomizer)
				.build();
	}

	@Bean
	public GroupedOpenApi prodGroup(@Value("${springdoc.version}") String appVersion) {
		return GroupedOpenApi.builder().group("Smart Mobility Recommender System API")
				.packagesToScan("gr.iccs.smart.mobility")
				.pathsToExclude("/api/scenario/**", "/api/graph/**", "/api/admin/**")
				.build();
	}
}
