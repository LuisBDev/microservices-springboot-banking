package com.msbanking.inventory_service.mapper;

import com.msbanking.inventory_service.dto.request.CreateProductRequest;
import com.msbanking.inventory_service.dto.request.UpdateProductRequest;
import com.msbanking.inventory_service.dto.response.ProductResponse;
import com.msbanking.inventory_service.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);
}
