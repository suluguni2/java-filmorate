package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService service;

    @GetMapping
    public List<Mpa> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable long id) {
        return service.findById(id);
    }
}
