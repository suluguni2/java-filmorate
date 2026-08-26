package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(long filmId);

    Film create(Film film);

    Film update(Film film);

    void clearFilms();

    void deleteFilm(long filmId);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    int getLikesCount(long filmId);

    List<Film> getPopularFilms(int count);
}
