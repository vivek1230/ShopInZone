package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.config.InventoryProjectionConfig;
import com.walmart.thor.endgame.orderingestion.dto.allocation.CreateAllocationRequest;
import com.walmart.thor.endgame.orderingestion.dto.allocation.CreateAllocationResponse;
import com.walmart.thor.endgame.orderingestion.exception.FMSOrderRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CreateAllocationService {

    private InventoryProjectionConfig inventoryProjectionConfig;
    private RestTemplate restTemplate;

    /**
     * This method calls create allocation rest api of Inventory BC, This call is idempotent
     *
     * @return CreateAllocationResponse
     */
    public CreateAllocationResponse callCreateAllocation(
            String fcId, CreateAllocationRequest createAllocationRequest) {

        log.info("Sending request to Inventory Projection. CreateAllocationRequest: {}", createAllocationRequest);
        ResponseEntity<CreateAllocationResponse> responseEntity;
        try {
            URI uri = inventoryProjectionConfig.getUri(fcId);
            log.info("uri= {} ", uri);
            HttpHeaders headers = inventoryProjectionConfig.getHttpHeaders();
            HttpEntity<CreateAllocationRequest> request = new HttpEntity<>(createAllocationRequest, headers);

            responseEntity = restTemplate.exchange(uri, HttpMethod.POST, request, CreateAllocationResponse.class);
            log.info("Response from Inventory Projection. CreateAllocationResponse= {} ", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (Exception ex) {
            throw new FMSOrderRuntimeException(ex.getMessage());
        }
    }
}