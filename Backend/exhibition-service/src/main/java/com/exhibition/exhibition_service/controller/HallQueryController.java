package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.model.Hall;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallQueryController {

    private final LayoutService layoutService;

    @GetMapping
    public ResponseEntity<List<Hall>> getHalls() {
        return ResponseEntity.ok(layoutService.getHalls());
    }
}
