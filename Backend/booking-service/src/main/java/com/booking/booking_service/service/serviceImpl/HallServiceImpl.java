package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.Hall;
import com.booking.booking_service.repository.HallRepository;
import com.booking.booking_service.service.HallService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HallServiceImpl implements HallService {

    @Autowired
    private HallRepository hallRepository;

    @Override
    public List<Hall> getAllHalls() {
        return hallRepository.findAll();
    }

    @Override
    public Hall createHall(Hall hall) {
        return hallRepository.save(hall);
    }

}
