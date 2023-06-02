package com.walmart.thor.endgame.orderingestion;

import com.azure.spring.autoconfigure.cosmos.CosmosHealthConfiguration;
import com.walmart.thor.endgame.orderingestion.common.config.CommonAppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;

@Import({CommonAppConfig.class})
@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class, CosmosHealthConfiguration.class
    },
    scanBasePackages = {
            "com.walmart.thor.endgame.orderingestion",
            "io.strati.tunr.utils.client"
    })
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
