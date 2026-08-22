package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    @Override
    public void clearFilms() {
        films.clear();
        filmLikes.clear();
        nextId = 1;
    }

    @Override
    public void deleteFilm(long filmId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        films.remove(filmId);
        filmLikes.remove(filmId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        Set<Long> userIds = filmLikes.get(filmId);
        if (userIds != null) {
            userIds.remove(userId);
        }
        log.info("Пользователь с id={} убирает лайк у фильма с id={}", userId, filmId);
    }

    @Override
    public int getLikesCount(long filmId) {
        log.info("Запрошено количество лайков фильма: {}", filmId);
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        Set<Long> userIds = filmLikes.get(filmId);
        if (userIds == null) {
            return 0;
        }
        log.debug("Количество лайков фильма: {} = {}", filmId, userIds.size());
        return userIds.size();
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("Запрошен список популярных фильмов, лимит: {}", count);
        return films.values().stream()
                .sorted(Comparator.comparingInt(
                        (Film film) ->
                                filmLikes.getOrDefault(film.getId(), Collections.emptySet()).size()
                ).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return films.values();
    }

    @Override
    public Film findById(long filmId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        return films.get(filmId);
    }

    @Override
    public Film create(Film film) {
        log.info("Получен запрос на создание фильма: {}", film.getName());

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Фильм успешно создан с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Получен запрос на обновление фильма с id={}", film.getId());

        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм с id={} успешно обновлён", film.getId());
        return film;
    }
}
