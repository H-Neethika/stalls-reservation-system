package com.booking.booking_service.controller;

import com.booking.booking_service.model.Genre;
import com.booking.booking_service.response.MessageResponse;
import com.booking.booking_service.service.GenreService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/genre")
public class GenreController {

  @Autowired
  private GenreService genreService;

  @PostMapping
  public ResponseEntity<Genre> createGenre(@RequestBody Genre genre) {
    Genre savedGenre = genreService.createGenre(genre);
    return new ResponseEntity<>(savedGenre, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Genre> updateGenre(@PathVariable Long id, @RequestBody Genre genre)
      throws Exception {
    Genre updatedGenre = genreService.updateGenre(id, genre);
    return new ResponseEntity<>(updatedGenre, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Genre> getGenreById(@PathVariable Long id)
      throws Exception {
    Genre genre = genreService.findGenreById(id);
    return new ResponseEntity<>(genre, HttpStatus.OK);
  }


  @DeleteMapping("/{id}")
  public ResponseEntity<MessageResponse> deleteGenreById(@PathVariable Long id)
      throws Exception {
    genreService.deleteGenre(id);
    MessageResponse response = new MessageResponse();
    response.setMessage("Genre deleted successfully.");
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  @GetMapping
  public ResponseEntity<List<Genre>> getAllGenres() {
    List<Genre> genreList = genreService.getAllGenres();
    return new ResponseEntity<>(genreList, HttpStatus.OK);
  }
}
