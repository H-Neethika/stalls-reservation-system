package com.booking.booking_service.response;

import com.booking.booking_service.domain.Role;
import lombok.Data;

@Data
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private String organizationName;
  private Role role;
}