package com.booking.booking_service.service.serviceImpl;

import com.booking.booking_service.model.Genre;
import com.booking.booking_service.repository.GenreRepository;
import com.booking.booking_service.service.GenreService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenreServiceImpl implements GenreService {

  @Autowired
  private GenreRepository genreRepository;

  @Override
  public Genre findGenreById(Long id) throws Exception {
    Optional<Genre> genre = genreRepository.findById(id);
    if (genre.isEmpty()) {
      throw new Exception("Genre not found with Id " + id);
    }
    return genre.get();
  }

  @Override
  public Genre createGenre(Genre genre) {
    Genre createdGenre = new Genre();
    createdGenre.setName(genre.getName());
    return genreRepository.save(createdGenre);
  }

  @Override
  public Genre updateGenre(Long id, Genre updateGenre) throws Exception {
    Genre genre = findGenreById(id);
    genre.setName(updateGenre.getName());
    return genreRepository.save(genre);
  }

  @Override
  public void deleteGenre(Long genreId) throws Exception {
    Genre genre = findGenreById(genreId);
    genreRepository.delete(genre);
  }
}
