package com.exhibition.exhibition_service.controller;


import com.exhibition.exhibition_service.dto.ExhibitionDTO;
import com.exhibition.exhibition_service.dto.ExhibitionWithHallsResponse;
import com.exhibition.exhibition_service.enums.ExhibitionState;
import com.exhibition.exhibition_service.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibition")
@RequiredArgsConstructor

public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @PostMapping
    public ResponseEntity<String> createExhibition(@RequestBody ExhibitionDTO exhibition){

        ExhibitionDTO createdExhibition=  exhibitionService.createExhibition(exhibition);

        return ResponseEntity.ok("Exhibition has been created");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateExhibition(
            @PathVariable Long id,
            @RequestBody ExhibitionDTO exhibition
    ){
        Long requesterUserId = exhibition.getOrganizerId();
        ExhibitionDTO updatedExhibition = exhibitionService.updateExhibition(id, exhibition, requesterUserId);
        return ResponseEntity.ok("Exhibition has been updated");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExhibition(@PathVariable Long id){
        exhibitionService.deleteExhibition(id);
        return ResponseEntity.ok("Exhibition has been deleted");


    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionDTO> getExhibition(@PathVariable Long id){

        return ResponseEntity.ok(exhibitionService.getExhibitionById(id));
    }

    @GetMapping
    public ResponseEntity<List<?>> getAllExhibitions(@RequestParam(value = "organizerId", required = false) Long organizerId){
        if (organizerId != null) {
            return ResponseEntity.ok(exhibitionService.getExhibitionsByOrganizer(organizerId));
        }
        return ResponseEntity.ok(exhibitionService.getAllExhibitions());
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<ExhibitionDTO>> getByState(@PathVariable ExhibitionState state) {
        return ResponseEntity.ok(exhibitionService.getExhibitionsByState(state));
    }
}
