package com.walmart.thor.endgame.orderingestion;

import com.azure.spring.autoconfigure.cosmos.CosmosHealthConfiguration;
import com.walmart.thor.endgame.orderingestion.common.config.CommonAppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(
        exclude = {
                 CosmosHealthConfiguration.class
        }, scanBasePackages = {
        "com.walmart.thor.endgame.orderingestion",
        "io.strati.tunr.utils.client"
        })
@Import({CommonAppConfig.class})

public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
