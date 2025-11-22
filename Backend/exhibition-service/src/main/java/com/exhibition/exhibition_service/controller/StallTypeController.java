package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.model.StallType;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stall-types")
@RequiredArgsConstructor
public class StallTypeController {

    private final LayoutService layoutService;

    @GetMapping
    public ResponseEntity<List<StallType>> getStallTypes() {
        return ResponseEntity.ok(layoutService.getStallTypes());
    }
}
