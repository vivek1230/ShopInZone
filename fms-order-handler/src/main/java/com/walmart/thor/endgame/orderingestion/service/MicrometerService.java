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
    String serviceName = "fms-order-handler";

    public Counter getAcceptedOrderMetric(String fcid, String lineStatus) {
        return registry.counter(ACCEPTED_ORDERS,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName,
                LINE_STATUS_KEY, lineStatus);
    }

    public Counter getRejectedOrderMetric(String fcid, String lineStatus) {
        return registry.counter(REJECTED_ORDERS,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName,
                LINE_STATUS_KEY, lineStatus);
    }

    public Counter getIgnoredCancellationMetric(String fcid) {
        return registry.counter(CUSTOMER_CANCEL_REQUEST_IGNORED,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName);
    }

    public Counter getPtCustomerCancelRequest(String fcid) {
        return registry.counter(PICKTICKET_CUSTOMER_CANCEL_REQUEST,
                FCID_KEY, fcid,
                SERVICE_NAME_KEY, serviceName);
    }
}
