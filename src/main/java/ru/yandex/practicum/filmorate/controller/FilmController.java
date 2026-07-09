package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final long MIN_DURATION = 1L;

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    public void clearFilms() {
        films.clear();
        nextId = 1;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());

        validateFilm(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Фильм успешно создан с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id={}", film.getId());

        if (film.getId() == null) {
            log.error("Id фильма не указан при обновлении");
            throw new ValidationException("Id фильма должен быть указан");
        }

        validateFilm(film);

        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм с id={} успешно обновлён", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Максимальная длина описания - {} символов", MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Максимальная длина описания - " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate()
                .isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() != null && film.getDuration() <= MIN_DURATION) {
            log.error("Продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}