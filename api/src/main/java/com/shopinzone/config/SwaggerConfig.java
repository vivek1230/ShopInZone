package com.walmart.thor.endgame.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(
        name = "springdoc.swagger-ui.enabled",
        havingValue = "true",
        matchIfMissing = true)
@ConfigurationProperties(prefix = "springdoc.swagger-ui.config")
@Setter
@Slf4j
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String GROUP_NAME = "Order Ingestion API v1";
    private static final String[] paths = {"/v1/**", "/inventory-projection/v1/**"};
    private String title;
    private String version;
    private String bearerUrl;

    @Value("${spring.security.auth-enabled}")
    private boolean isAuthEnabled;

    @Bean
    public OpenAPI customOpenAPI() {
        Schema<LocalTime> localTimeSchema = new Schema<>();
        localTimeSchema.setType("string");
        localTimeSchema.setExample("00:00:00");

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .info(new Info().title(title).version(version))
                .schema("LocalTime", localTimeSchema);
    }

    @Bean
    public GroupedOpenApi storeOpenApi(List<OpenApiCustomiser> customiserList) {
        var openAPIBuilder = GroupedOpenApi.builder().group(GROUP_NAME).pathsToMatch(paths);
        customiserList.forEach(openAPIBuilder::addOpenApiCustomiser);
        return openAPIBuilder.build();
    }

    @Bean
    public OpenApiCustomiser openApiCustomiser(Map<String, Example> examples) {
        return openAPI ->
                examples.forEach((key, value) -> openAPI.getComponents().addExamples(key, value));
    }

    @Bean
    @ConditionalOnProperty(
            name = {"spring.security.auth-enabled"},
            havingValue = "true")
    public OpenApiCustomiser openApiSecurityCustomiser() {
        var securitySchema =
                new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");
        return openAPI ->
                openAPI.getComponents().addSecuritySchemes(SECURITY_SCHEME_NAME, securitySchema);
    }

    @Bean
    @ConditionalOnProperty(
            name = {"spring.security.auth-enabled"},
            havingValue = "true")
    public OpenApiCustomiser openApiContactInfoCustomiser() {
        Contact contact = new Contact();
        contact.setName("Bearer Token");
        contact.setUrl(bearerUrl);
        return openAPI -> openAPI.getInfo().setContact(contact);
    }
}
