package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    final private FilmStorage filmStorage;
    final private UserStorage userStorage;

    public void addLike(long filmId, long userId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        if (!userStorage.isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public int getLikesCount(long filmId) {
        return filmStorage.getLikesCount(filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}
