package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String SELECT = "SELECT f.*, m.name AS mpa_name FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.id ";
    private static final String INSERT = "INSERT INTO films(name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {
        return loadGenres(findMany(SELECT + "ORDER BY f.id"));
    }

    @Override
    public Film findById(long filmId) {
        Film film = findOne(SELECT + "WHERE f.id = ?", filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        return loadGenres(List.of(film)).getFirst();
    }

    @Override
    @Transactional
    public Film create(Film film) {
        film.setId(insert(INSERT, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() == null ? null : film.getMpa().getId()));
        saveGenres(film);
        return findById(film.getId());
    }

    @Override
    @Transactional
    public Film update(Film film) {
        update(UPDATE, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(), film.getId());
        jdbc.update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return findById(film.getId());
    }

    @Override
    public void clearFilms() {
        jdbc.update("DELETE FROM films");
    }

    @Override
    public void deleteFilm(long filmId) {
        update("DELETE FROM films WHERE id = ?", filmId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        findById(filmId);
        jdbc.update("MERGE INTO films_likes(film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        findById(filmId);
        jdbc.update("DELETE FROM films_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public int getLikesCount(long filmId) {
        findById(filmId);
        return jdbc.queryForObject("SELECT COUNT(*) FROM films_likes WHERE film_id = ?", Integer.class, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return loadGenres(findMany(SELECT + "LEFT JOIN (SELECT film_id, COUNT(*) AS likes FROM films_likes " +
                "GROUP BY film_id) l ON f.id = l.film_id ORDER BY COALESCE(l.likes, 0) DESC, f.id LIMIT ?", count));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("MERGE INTO films_genres(film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }

    private List<Film> loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        Map<Long, Film> byId = films.stream().collect(Collectors.toMap(Film::getId, Function.identity()));
        String placeholders = films.stream().map(film -> "?").collect(Collectors.joining(","));
        jdbc.query("SELECT fg.film_id, g.id, g.name FROM films_genres fg JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") ORDER BY g.id", rs -> {
                    Film film = byId.get(rs.getLong("film_id"));
                    film.getGenres().add(new Genre(rs.getLong("id"), rs.getString("name")));
                }, films.stream().map(Film::getId).toArray());
        return films;
    }
}
