package com.booking.booking_service.service;

import com.booking.booking_service.model.Hall;
import java.util.List;

public interface HallService {

  public List<Hall> getAllHalls();

  public  Hall createHall(Hall hall);

  public Hall updateHall(Long id, Hall hall);
  public void deleteHall(Long id);
}
