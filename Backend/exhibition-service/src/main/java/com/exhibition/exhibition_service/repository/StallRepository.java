package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.Hall;
import com.exhibition.exhibition_service.model.Stall;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StallRepository extends JpaRepository<Stall, Long> {
    List<Stall> findByHall(Hall hall);
}
