package com.walmart.thor.endgame.orderingestion.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fms-configs")
@Setter
@Getter
public class FCDetailsConfig {
  private Map<String, String> shipNodeToFcidMap;
}
