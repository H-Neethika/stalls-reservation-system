package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.dto.AssignStallRequest;
import com.exhibition.exhibition_service.dto.CreateExhibitionHallRequest;
import com.exhibition.exhibition_service.dto.CreateStallRequest;
import com.exhibition.exhibition_service.dto.CreateStallTypeRequest;
import com.exhibition.exhibition_service.dto.ExhibitionHallPriceResponse;
import com.exhibition.exhibition_service.dto.HallLayoutResponse;
import com.exhibition.exhibition_service.dto.OrganizerLayoutResponse;
import com.exhibition.exhibition_service.dto.StallStatusResponse;
import com.exhibition.exhibition_service.dto.StallSummaryResponse;
import com.exhibition.exhibition_service.dto.UpdateStallStatusRequest;
import com.exhibition.exhibition_service.model.ExhibitionHall;
import com.exhibition.exhibition_service.model.ExhibitionStall;
import com.exhibition.exhibition_service.model.Hall;
import com.exhibition.exhibition_service.model.Stall;
import com.exhibition.exhibition_service.service.LayoutService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/layout")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    @PostMapping("/halls")
    public ResponseEntity<Hall> createHall(@RequestBody Hall hall) {
        return ResponseEntity.ok(layoutService.createHall(hall.getHallName()));
    }

    @GetMapping("/halls")
    public ResponseEntity<List<Hall>> getHalls() {
        return ResponseEntity.ok(layoutService.getHalls());
    }

    @GetMapping("/stall-types")
    public ResponseEntity<List<com.exhibition.exhibition_service.model.StallType>> getStallTypes() {
        return ResponseEntity.ok(layoutService.getStallTypes());
    }

    @GetMapping("/halls/{hallId}")
    public ResponseEntity<HallLayoutResponse> getHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(layoutService.getHallLayout(hallId));
    }

    @PostMapping("/exhibitions/{exhibitionId}/halls")
    public ResponseEntity<ExhibitionHall> createExhibitionHall(@PathVariable Long exhibitionId,
                                                               @RequestBody CreateExhibitionHallRequest request) {
        request.setExhibitionId(exhibitionId);
        return ResponseEntity.ok(layoutService.createExhibitionHall(request));
    }

    @GetMapping("/exhibitions/{exhibitionId}/halls")
    public ResponseEntity<List<ExhibitionHall>> getExhibitionHalls(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(layoutService.getExhibitionHalls(exhibitionId));
    }

    @GetMapping("/organizers/{organizerId}")
    public ResponseEntity<OrganizerLayoutResponse> getLayoutByOrganizer(@PathVariable Long organizerId) {
        return ResponseEntity.ok(layoutService.getOrganizerLayout(organizerId));
    }

    @PostMapping("/stall-types")
    public ResponseEntity<com.exhibition.exhibition_service.model.StallType> createStallType(@RequestBody CreateStallTypeRequest request) {
        return ResponseEntity.ok(layoutService.createStallType(request));
    }

    @GetMapping("/halls/{exhibitionHallId}/stall-types")
    public ResponseEntity<List<ExhibitionHallPriceResponse>> getStallTypes(@PathVariable Long exhibitionHallId) {
        return ResponseEntity.ok(layoutService.getStallTypes(exhibitionHallId));
    }

    @PostMapping("/stalls")
    public ResponseEntity<Stall> createStall(@RequestBody CreateStallRequest request) {
        return ResponseEntity.ok(layoutService.createStall(request));
    }

    @GetMapping("/halls/{hallId}/stalls")
    public ResponseEntity<List<Stall>> getStallsByHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(layoutService.getStallsByHall(hallId));
    }

    @PostMapping("/stalls/assign")
    public ResponseEntity<ExhibitionStall> assignStall(@RequestBody AssignStallRequest request) {
        return ResponseEntity.ok(layoutService.assignStallToExhibition(request));
    }

    @GetMapping("/exhibitions/{exhibitionId}/stalls")
    public ResponseEntity<List<ExhibitionStall>> getStallsForExhibition(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(layoutService.getStallsForExhibition(exhibitionId));
    }

    @GetMapping("/stalls/summary")
    public ResponseEntity<List<StallSummaryResponse>> getStallSummary(@RequestParam("ids") String ids) {
        List<Long> stallIds = Arrays.stream(ids.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return ResponseEntity.ok(layoutService.getStallSummaries(stallIds));
    }

    @PostMapping("/exhibitions/{exhibitionId}/stalls/reserve")
    public ResponseEntity<List<StallStatusResponse>> reserve(@PathVariable Long exhibitionId,
                                                             @RequestBody UpdateStallStatusRequest request) {
        return ResponseEntity.ok(layoutService.reserveStalls(exhibitionId, request));
    }

    @PostMapping("/exhibitions/{exhibitionId}/stalls/release")
    public ResponseEntity<List<StallStatusResponse>> release(@PathVariable Long exhibitionId,
                                                             @RequestBody UpdateStallStatusRequest request) {
        return ResponseEntity.ok(layoutService.releaseStalls(exhibitionId, request));
    }

    @GetMapping("/exhibitions/{exhibitionId}/stalls/status")
    public ResponseEntity<List<StallStatusResponse>> status(@PathVariable Long exhibitionId,
                                                            @RequestParam("ids") String ids) {
        List<Long> stallIds = Arrays.stream(ids.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return ResponseEntity.ok(layoutService.getStatuses(exhibitionId, stallIds));
    }
}
