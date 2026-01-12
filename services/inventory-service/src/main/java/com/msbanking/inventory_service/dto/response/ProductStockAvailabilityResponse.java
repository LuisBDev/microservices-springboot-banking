package com.msbanking.inventory_service.dto.response;

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
