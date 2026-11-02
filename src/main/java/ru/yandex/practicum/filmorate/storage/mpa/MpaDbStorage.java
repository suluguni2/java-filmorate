package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;

@Repository
public class MpaDbStorage extends BaseRepository<Mpa> {
    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Mpa> findAll() {
        return findMany("SELECT * FROM mpa ORDER BY id");
    }

    public Mpa findById(long id) {
        return findOne("SELECT * FROM mpa WHERE id = ?", id)
                .orElseThrow(() -> new NotFoundException("Mpa с id=" + id + " не найден"));
    }
}
