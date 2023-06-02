package com.walmart.thor.endgame.configs;

import com.azure.spring.aad.webapi.AADJwtBearerTokenAuthenticationConverter;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@EnableWebSecurity
@Configuration
@ConfigurationProperties(prefix = "spring.security")
@Setter
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private String apiAllowedOrigins;
    private boolean authEnabled;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (authEnabled) {
            http.csrf()
                    .disable()
                    .cors()
                    .and()
                    .authorizeRequests(
                            requests ->
                                    requests
                                            .antMatchers(
                                                    "/actuator/**",
                                                    "/swagger*/**",
                                                    "/v3/api-docs",
                                                    "/webjars/**",
                                                    "/configuration/**")
                                            .permitAll()
                                            .anyRequest()
                                            .authenticated())
                    .oauth2ResourceServer()
                    .jwt()
                    .jwtAuthenticationConverter(new AADJwtBearerTokenAuthenticationConverter());
        } else {
            http.cors().and().csrf().disable().authorizeRequests().anyRequest().permitAll();
            SecurityContextHolder.getContext().setAuthentication(getTestAuthToken());
        }
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger.html", "/swagger-ui/**");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
        corsConfiguration.setAllowedOrigins(List.of(CorsConfiguration.ALL));
        corsConfiguration.setAllowedMethods(List.of(CorsConfiguration.ALL));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    private TestingAuthenticationToken getTestAuthToken() {
        return new TestingAuthenticationToken("testuser_name@example.com", "");
    }
}
