package com.walmart.thor.endgame.orderingestion.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

@Configuration
@ConfigurationProperties(prefix = "inventory-projection")
@Setter
@Getter
@Slf4j
public class InventoryProjectionConfig {

  private String protocol;
  private String host;
  private String port;
  private String endpoint;
  private String securedEndpoint;
  private String authHeaderName;
  private String authToken;
  private Boolean isAuthEnabled;

  public URI getUri(String fcId) {
    if (isAuthEnabled)
      return UriComponentsBuilder.newInstance()
          .scheme(protocol)
          .host(host)
          .port(port)
          .path(securedEndpoint)
          .buildAndExpand(fcId)
          .toUri();
    else
      return UriComponentsBuilder.newInstance()
          .scheme(protocol)
          .host(host)
          .port(port)
          .path(endpoint)
          .buildAndExpand(fcId)
          .toUri();
  }

  @Bean
  RestTemplate getRestTemplate() {
    return new RestTemplate();
  }

  public HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(getAuthHeaderName(), getAuthToken());
    return headers;
  }
}
