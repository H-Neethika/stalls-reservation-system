package com.booking.booking_service.service;

import com.booking.booking_service.model.Genre;
import java.util.List;

public interface GenreService {

  public Genre findGenreById(Long id) throws Exception;

  public Genre createGenre(Genre genre);

  public Genre updateGenre(Long id, Genre updateGenre) throws Exception;

  public void deleteGenre(Long genreId) throws Exception;

  public List<Genre> getAllGenres();
}
