package com.walmart.thor.endgame.orderingestion.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.walmart.thor.endgame.orderingestion.common.config.serdes.CustomOffsetDateTimeSerializer;
import com.walmart.thor.endgame.orderingestion.common.dto.fmsorder.FmsOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;

@Jacksonized
@Builder
@Getter
@FieldDefaults(makeFinal = true)
@ToString
public class FmsOrderEvent {

  String eventName;
  String eventSource;
  String eventId;
  FmsOrder eventPayload;
  @JsonSerialize(using = CustomOffsetDateTimeSerializer.class)
  OffsetDateTime eventTime;
}
