package com.exhibition.exhibition_service.controller;

import com.exhibition.exhibition_service.model.Genre;
import com.exhibition.exhibition_service.service.GenreService;
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
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<Genre> create(@RequestBody Genre genre) {
        return ResponseEntity.ok(genreService.create(genre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Genre> update(@PathVariable Long id, @RequestBody Genre genre) {
        return ResponseEntity.ok(genreService.update(id, genre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> find(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.find(id));
    }

    @GetMapping
    public ResponseEntity<List<Genre>> all() {
        return ResponseEntity.ok(genreService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stall/{id}")
    public ResponseEntity<Genre> findByStallId(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getGenreByStallId(id));
    }


}
