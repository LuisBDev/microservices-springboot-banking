package com.msbanking.orders_service.client;

import com.msbanking.orders_service.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service")
public interface UsersClient {

    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
