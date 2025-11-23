package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.dto.HallLayoutResponse;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/layout")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    @GetMapping("/halls/{hallId}")
    public ResponseEntity<HallLayoutResponse> getHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(layoutService.getHallLayout(hallId));
    }

    @GetMapping("/halls")
    public ResponseEntity<List<HallLayoutResponse>> getAllHalls() {
        return ResponseEntity.ok(layoutService.getAllHallLayouts());
    }

    @GetMapping("/exhibitions/{exhibitionId}")
    public ResponseEntity<List<com.exhibition.exhibition_service.dto.ExhibitionHallLayoutResponse>> getExhibitionHallsLayout(
            @PathVariable Long exhibitionId) {
        return ResponseEntity.ok(layoutService.getExhibitionHallLayouts(exhibitionId));
    }
}
