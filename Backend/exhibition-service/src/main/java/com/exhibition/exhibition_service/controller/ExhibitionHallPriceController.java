package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.dto.CreateExhibitionHallPriceRequest;
import com.exhibition.exhibition_service.dto.ExhibitionHallPriceResponse;
import com.exhibition.exhibition_service.model.ExhibitionHallPrice;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hall-prices")
@RequiredArgsConstructor
public class ExhibitionHallPriceController {

    private final LayoutService layoutService;

    @PostMapping
    public ResponseEntity<ExhibitionHallPrice> create(@RequestBody CreateExhibitionHallPriceRequest request) {
        return ResponseEntity.ok(layoutService.createHallPrice(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExhibitionHallPrice> update(@PathVariable Long id,
                                                      @RequestBody CreateExhibitionHallPriceRequest request) {
        return ResponseEntity.ok(layoutService.updateHallPrice(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionHallPriceResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(layoutService.getHallPrice(id));
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<ExhibitionHallPriceResponse>> byHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(layoutService.getHallPricesByHall(hallId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        layoutService.deleteHallPrice(id);
        return ResponseEntity.noContent().build();
    }
}
