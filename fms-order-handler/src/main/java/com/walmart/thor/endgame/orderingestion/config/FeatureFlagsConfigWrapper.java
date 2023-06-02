package com.walmart.thor.endgame.orderingestion.config;

import com.walmart.thor.endgame.orderingestion.common.config.ccm.FeatureFlagsConfig;
import com.walmart.thor.endgame.orderingestion.service.orderhandler.FmsOrderHandler;
import io.strati.ccm.utils.client.annotation.ManagedConfiguration;
import io.strati.ccm.utils.client.annotation.PostRefresh;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeatureFlagsConfigWrapper {

    private final Map<String, FmsOrderHandler> fmsOrderHandlers;

    @ManagedConfiguration
    FeatureFlagsConfig featureFlagsConfig;

    @Value("${inventory-projection.whitelisted-fc}")
    private Set<String> ipBcWhitelistedFc;

    public FeatureFlagsConfigWrapper(Map<String, FmsOrderHandler> fmsOrderHandlers) {
        this.fmsOrderHandlers = fmsOrderHandlers;
    }

    @PostRefresh(configName = "ALL")
    public void onConfigUpdate() {
        log.info("CCM2 config featureFlagsConfig enableIpBc changed to {}",
                featureFlagsConfig.enableIpBc());
    }

    public FmsOrderHandler getFmsOrderHandler(String fcId) {
        if(featureFlagsConfig.enableIpBc() && ipBcWhitelistedFc.contains(fcId)){
            log.info("Calling FmsOrderHandlerInventoryProjection flow");
            return fmsOrderHandlers.get("fmsOrderHandlerInventoryProjection");
        }
        log.info("Calling FmsOrderHandlerDefault flow");
        return fmsOrderHandlers.get("fmsOrderHandlerDefault");
    }

    public Boolean isCustomerCancellationEnabled() {
        return featureFlagsConfig.enableCustomerCancel();
    }

}
