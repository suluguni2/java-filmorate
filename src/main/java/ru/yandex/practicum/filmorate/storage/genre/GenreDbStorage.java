package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;

@Repository
public class GenreDbStorage extends BaseRepository<Genre> {
    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        return findMany("SELECT * FROM genres ORDER BY id");
    }

    public Genre findById(long id) {
        return findOne("SELECT * FROM genres WHERE id = ?", id)
                .orElseThrow(() -> new NotFoundException("Genre с id=" + id + " не найден"));
    }
}
