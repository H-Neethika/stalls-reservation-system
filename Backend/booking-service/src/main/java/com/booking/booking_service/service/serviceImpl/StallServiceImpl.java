package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.Stall;
import com.booking.booking_service.repository.StallRepository;
import com.booking.booking_service.service.StallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StallServiceImpl implements StallService {


    @Autowired
    private StallRepository stallRepository;


    @Override
    public List<Stall> findAllByHallId(Long hallId) {

        List<Stall> stalls = stallRepository.findAllByHallId(hallId);
        return stalls;
    }
}
