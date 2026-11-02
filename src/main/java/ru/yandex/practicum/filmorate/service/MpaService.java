package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage storage;

    public List<Mpa> findAll() {
        return storage.findAll();
    }

    public Mpa findById(long id) {
        return storage.findById(id);
    }
}
