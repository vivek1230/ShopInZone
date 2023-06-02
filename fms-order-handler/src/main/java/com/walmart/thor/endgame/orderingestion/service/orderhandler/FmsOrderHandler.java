package com.walmart.thor.endgame.orderingestion.service.orderhandler;

import com.walmart.thor.endgame.orderingestion.dto.FmsOrderEvent;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderException;

public interface FmsOrderHandler {
    void process(FmsOrderEvent fmsOrderEvent) throws FMSOrderException;
}
