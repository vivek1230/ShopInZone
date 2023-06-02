package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.common.dto.domain.LineStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MicrometerServiceTest {

    MicrometerService micrometerService;

    MeterRegistry meterRegistry = Mockito.mock(MeterRegistry.class);
    Counter counter = Mockito.mock(Counter.class);

    @BeforeEach
    void setUp() {

        micrometerService = new MicrometerService(meterRegistry);
    }

    @Test
    void getAcceptedOrderMetric() {
        when(meterRegistry.counter(any(), any(), any(), any(), any(), any(), any())).thenReturn(counter);
        String fcid = "9610";
        String lineStatus = LineStatus.LW.getCode();

        micrometerService.getAcceptedOrderMetric(fcid, lineStatus);
        verify(meterRegistry, atLeastOnce()).counter(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getRejectedOrderMetric() {
        when(meterRegistry.counter(any(), any(), any(), any(), any(), any(), any())).thenReturn(counter);
        String fcid = "9610";
        String lineStatus = LineStatus.LB.getCode();

        micrometerService.getRejectedOrderMetric(fcid, lineStatus);
        verify(meterRegistry, atLeastOnce()).counter(any(), any(), any(), any(), any(), any(), any());
    }
}