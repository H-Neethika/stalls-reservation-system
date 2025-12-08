package com.exhibition.exhibition_service.service;

import com.exhibition.exhibition_service.model.Genre;
import java.util.List;

public interface GenreService {
    Genre create(Genre genre);
    Genre update(Long id, Genre genre);
    Genre find(Long id);
    void delete(Long id);
    List<Genre> findAll();
    public Genre getGenreByStallId(Long id);
    public List<Genre> createGenres(List<Genre> genres);

}
