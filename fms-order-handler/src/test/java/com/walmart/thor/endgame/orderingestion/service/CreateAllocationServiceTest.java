package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.config.InventoryProjectionConfig;
import com.walmart.thor.endgame.orderingestion.dto.allocation.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CreateAllocationServiceTest {

    public static final String FC_ID = "9610";
    public static final String PURCHASE_ORDER_NUMBER = "1661142011443";
    public static final String PICK_TICKET_ID = "11A";
    public static final String GTIN = "00032231512786";
    public static final String SELLER_ID = "F55CDC31AB754BB68FE0B39041159D63";
    public static final String AREA = "Undefined";
    CreateAllocationService createAllocationService;

    @MockBean
    private InventoryProjectionConfig inventoryProjectionConfig;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeAll
    void setup() {
        createAllocationService = new CreateAllocationService(inventoryProjectionConfig, restTemplate);
    }

    @Test
    void callCreateAllocation() {

        Mockito.when(inventoryProjectionConfig.getUri(FC_ID)).thenReturn(getUri());
        when(restTemplate.exchange(any(), any(), any(), (Class<Object>) any())).thenReturn(ResponseEntity.ok(getCreateAllocationResponse()));

        CreateAllocationResponse createAllocationResponse = createAllocationService.callCreateAllocation(FC_ID, getCreateAllocationRequest());
        verify(restTemplate, atLeastOnce()).exchange(any(), any(), any(), (Class<Object>) any());
        Assertions.assertThat(createAllocationResponse).isNotNull();
    }

    private URI getUri() {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port("8080")
                .path("/inventory-projection/v1/{fcId}/create-allocation")
                .buildAndExpand(FC_ID).toUri();
    }

    private CreateAllocationRequest getCreateAllocationRequest() {
        List<PickTicket> pickTicketList = new ArrayList<>();
        List<Item> itemList = new ArrayList<>();

        ItemKey itemKey = ItemKey.builder()
                .fcid(FC_ID).gtin(GTIN).sellerId(SELLER_ID).build();
        itemList.add(Item.builder()
                .quantity(10).itemKey(itemKey).build());
        pickTicketList.add(PickTicket.builder()
                .itemList(itemList).pickTicketId(PICK_TICKET_ID).build());
        return CreateAllocationRequest.builder()
                .pickTicketList(pickTicketList).purchaseOrderNumber(PURCHASE_ORDER_NUMBER).build();
    }

    private CreateAllocationResponse getCreateAllocationResponse() {
        List<Allocation> allocationList = new ArrayList<>();

        Allocation allocation = Allocation.builder()
                .pickTicketId(PICK_TICKET_ID).purchaseOrderNumber(PURCHASE_ORDER_NUMBER)
                .gtin(GTIN).fcid(FC_ID).id(123).area(AREA)
                .qty(40).sellerId(SELLER_ID).build();
        allocationList.add(allocation);
        Success success = Success.builder()
                .allocationList(allocationList).build();
        return CreateAllocationResponse.builder()
                .success(success).build();
    }
}