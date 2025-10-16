package com.booking.booking_service.repository;

import com.booking.booking_service.model.Stall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StallRepository extends JpaRepository<Stall, Long> {

    public List<Stall> findAllByHallId(Long hallId);

}
