package com.booking.booking_service.service;

import com.booking.booking_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", url = "${USER_SERVICE_BASE_URL:http://localhost:9090}", path = "/api/users")
public interface UserService {

  @GetMapping("/{id}")
  UserResponse getUserById(@PathVariable("id") Long id);
}
