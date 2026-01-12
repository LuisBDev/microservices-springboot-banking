package com.msbanking.orders_service.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Luis Balarezo
 **/
@Getter
@Setter
@Builder
public class ProductStockAvailabilityResponse {

    private String sku;
    private Integer requestedQuantity;
    private Boolean available;

}
