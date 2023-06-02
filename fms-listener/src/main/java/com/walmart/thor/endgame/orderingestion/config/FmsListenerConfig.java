package com.walmart.thor.endgame.orderingestion.config;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "listener-config")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class FmsListenerConfig {
  Map<String, String> shipNodeToFcid;
}
