package com.booking.booking_service.service;

import com.booking.booking_service.model.Stall;

import java.util.List;

public interface StallService  {

    public List<Stall> findAllByHallId(Long hallId);

}
