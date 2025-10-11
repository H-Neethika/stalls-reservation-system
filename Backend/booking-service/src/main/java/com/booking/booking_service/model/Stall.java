package com.booking.booking_service.model;

import com.booking.booking_service.domain.BOOKING_STATUS;
import com.booking.booking_service.domain.STALL_TYPE;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;

@Entity
public class Stall {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String stallName;
  private Long hallId;
  private STALL_TYPE stallType;
  private BOOKING_STATUS bookingStatus;
  private Boolean isActive;
  private List<String> genre;
  private Long price;

}
