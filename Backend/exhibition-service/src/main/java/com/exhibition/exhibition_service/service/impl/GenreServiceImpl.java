package com.exhibition.exhibition_service.service.impl;

import com.exhibition.exhibition_service.model.Genre;
import com.exhibition.exhibition_service.repository.GenreRepository;
import com.exhibition.exhibition_service.service.GenreService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public Genre create(Genre genre) {
        return genreRepository.save(genre);
    }

    @Override
    public Genre update(Long id, Genre genre) {
        Genre existing = find(id);
        existing.setName(genre.getName());
        return genreRepository.save(existing);
    }

    @Override
    public Genre find(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id));
    }

    @Override
    public void delete(Long id) {
        genreRepository.delete(find(id));
    }

    @Override
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    @Override
    public Genre getGenreByStallId(Long id) {
        return genreRepository.findByStallId(id);
    }
}
