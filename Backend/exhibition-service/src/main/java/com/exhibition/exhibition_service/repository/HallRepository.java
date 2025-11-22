package com.exhibition.exhibition_service.repository;

import com.exhibition.exhibition_service.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<Hall, Long> {
}
