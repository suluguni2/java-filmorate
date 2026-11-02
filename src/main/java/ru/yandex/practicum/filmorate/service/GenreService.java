package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage storage;

    public List<Genre> findAll() {
        return storage.findAll();
    }

    public Genre findById(long id) {
        return storage.findById(id);
    }
}
