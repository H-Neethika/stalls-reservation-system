package com.booking.booking_service.dto.response;

import com.booking.booking_service.enums.Role;
import lombok.Data;

@Data
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private String organizationName;
  private Role role;
}
