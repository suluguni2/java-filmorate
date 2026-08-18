package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    final InMemoryFilmStorage filmStorage;

    public void clearFilms() {
        filmStorage.clearFilms();
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        return filmStorage.update(film);
    }
}