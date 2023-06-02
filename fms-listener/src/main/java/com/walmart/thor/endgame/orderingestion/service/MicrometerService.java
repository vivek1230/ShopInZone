package com.walmart.thor.endgame.orderingestion.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.walmart.thor.endgame.orderingestion.common.constants.BuisnessMetrics.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class MicrometerService {

    MeterRegistry registry;
    String serviceName = "fms-listener";

    public Counter getOrdersWithValidFCMetric(String fcid) {
        return registry.counter(ORDERS_WITH_VALID_FC,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName);
    }

    public Counter getOrdersUnparsableMetric() {
        return registry.counter(ORDERS_WITH_UNPARSABLE_DATA,
                SERVICE_NAME_KEY, serviceName);
    }

    public Counter getValidOrdersMetric(String fcid, String lineStatus) {
        return registry.counter(ORDERS_WITH_VALID_DETAILS,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName,
                LINE_STATUS_KEY, lineStatus);
    }

    public Counter getInvalidOrdersMetric(String fcid, String lineStatus) {
        return registry.counter(ORDERS_WITH_INVALID_DETAILS,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName,
                LINE_STATUS_KEY, lineStatus);
    }

    public Counter getInvalidGtinMetric(String fcid, String lineStatus) {
        return registry.counter(ORDERS_WITH_INVALID_GTIN,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName,
                LINE_STATUS_KEY, lineStatus);
    }

    public Counter getCustomerCancelMetric(String fcid) {
        return registry.counter(CUSTOMER_CANCEL_REQUEST,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName);
    }

    public Counter getIgnoredCancellationMetric(String fcid) {
        return registry.counter(CUSTOMER_CANCEL_REQUEST_IGNORED,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName);
    }

}
