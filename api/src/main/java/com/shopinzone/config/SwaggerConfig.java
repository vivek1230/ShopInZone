package com.shopinzone.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import lombok.Setter;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "springdoc.swagger-ui.config")
@Setter
public class SwaggerConfig {

    private static final String GROUP_NAME = "API v1";
    // private static final String[] paths = { "/admin/v1/**", "/customer/v1/**", "/retailer/v1/**", "/firstApp/v1/**"
    // };
    private static final String[] paths = { "/firstApp/v1/**" };
    private String title;
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        Schema<LocalTime> localTimeSchema = new Schema<>();
        localTimeSchema.setType("string");
        localTimeSchema.setExample("00:00:00");

        OpenAPI openAPI = new OpenAPI().info(new Info().title(title).version(version));
        openAPI.schema("LocalTime", localTimeSchema);
        return openAPI;
    }

    @Bean
    public GroupedOpenApi storeOpenApi() {
        return GroupedOpenApi.builder().group(GROUP_NAME).pathsToMatch(paths).build();
    }
}
