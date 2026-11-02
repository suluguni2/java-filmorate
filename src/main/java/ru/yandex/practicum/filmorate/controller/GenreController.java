package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService service;

    @GetMapping
    public List<Genre> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable long id) {
        return service.findById(id);
    }
}
