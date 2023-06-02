package com.shopinzone.controllers;

import com.walmart.thor.endgame.exceptions.OrderIngestionException;
import com.walmart.thor.endgame.fms.FMSRequest;
import com.walmart.thor.endgame.fms.FMSRequestRandom;
import com.walmart.thor.endgame.fms.FMSResponse;
import com.walmart.thor.endgame.services.FmsOrderGeneratorRandomService;
import com.walmart.thor.endgame.services.FmsOrderGeneratorService;
import com.walmart.thor.endgame.services.FociResponseGenerator;
import com.walmart.thor.endgame.services.OrderIngestionService;
import com.walmart.thor.endgame.udc.UDCRequest;
import com.walmart.thor.endgame.udc.UDCResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

@Slf4j
@Validated
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1")
@Tag(name = "Order Ingestion API")
public class OrderIngestionController {

  private static final String FC_ID_IS_REQUIRED = "fcId is required";
  private static final String PURCHASE_ORDER_NO_IS_REQUIRED = "purchaseOrderNo is required";

  private final OrderIngestionService orderIngestionService;
  private final FociResponseGenerator fociResponseGenerator;
  private final FmsOrderGeneratorService fmsOrderGeneratorService;
  private final FmsOrderGeneratorRandomService fmsOrderGeneratorRandomService;

  @Autowired
  public OrderIngestionController(
      OrderIngestionService orderIngestionService,
      FociResponseGenerator fociResponseGenerator,
      FmsOrderGeneratorService fmsOrderGeneratorService,
      FmsOrderGeneratorRandomService fmsOrderGeneratorRandomService) {

    this.orderIngestionService = orderIngestionService;
    this.fociResponseGenerator = fociResponseGenerator;
    this.fmsOrderGeneratorService = fmsOrderGeneratorService;
    this.fmsOrderGeneratorRandomService = fmsOrderGeneratorRandomService;
  }

  @GetMapping("/fcId/{fcId}/getPickTicketList/{purchaseOrderNo}")
  public ResponseEntity<List<String>> getPickTicketListWithPurchaseOrderNo(
      @PathVariable @NotBlank(message = FC_ID_IS_REQUIRED) String fcId,
      @PathVariable @NotBlank(message = PURCHASE_ORDER_NO_IS_REQUIRED) String purchaseOrderNo)
      throws OrderIngestionException {

    List<String> pickTicketIds = orderIngestionService.getPickTicketList(fcId, purchaseOrderNo);
    return new ResponseEntity<>(pickTicketIds, HttpStatus.OK);
  }

  @PostMapping(path = "/fcId/{fcId}/create-mass-order")
  public ResponseEntity<FMSResponse> createMassOrders(
      @PathVariable String fcId, @Validated @RequestBody FMSRequest fmsRequest)
      throws IOException, OrderIngestionException {

    FMSResponse fmsResponse = fmsOrderGeneratorService.createMassOrders(fcId, fmsRequest);
    return new ResponseEntity<>(fmsResponse, HttpStatus.OK);
  }

  @PostMapping(path = "/fcId/{fcId}/create-mass-order-random")
  public ResponseEntity<FMSResponse> createMassOrderRandom(
      @PathVariable String fcId, @Validated @RequestBody FMSRequestRandom fmsRequestRandom)
      throws IOException, OrderIngestionException {

    FMSResponse fmsResponse =
        fmsOrderGeneratorRandomService.createMassOrderRandom(fcId, fmsRequestRandom);
    return new ResponseEntity<>(fmsResponse, HttpStatus.OK);
  }

  @PostMapping(path = "/mockFoci", produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody UDCResponse mockFoci(@RequestBody UDCRequest udcRequest) throws IOException {
    return fociResponseGenerator.getUdcResponse(udcRequest);
  }
}
