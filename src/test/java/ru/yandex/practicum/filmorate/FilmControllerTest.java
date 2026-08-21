package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController.clearFilms();
    }

    @Test
    void createFilm_withEmptyName_shouldReturnError() throws Exception {
        Film film = new Film(null, "", "Описание",
                LocalDate.of(2020, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_withValidName_shouldReturnCreated() throws Exception {
        Film film = new Film(null, "A", "Описание",
                LocalDate.of(2020, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A"));
    }

    @Test
    void createFilm_withDescription200Chars_shouldReturnCreated() throws Exception {
        String description = "a".repeat(200);
        Film film = new Film(null, "Фильм", description,
                LocalDate.of(2020, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(description));
    }

    @Test
    void createFilm_withDescription201Chars_shouldReturnError() throws Exception {
        String description = "a".repeat(201);
        Film film = new Film(null, "Фильм", description,
                LocalDate.of(2020, 1, 1), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_withReleaseDate28Dec1895_shouldReturnCreated() throws Exception {
        Film film = new Film(null, "Фильм", "Описание",
                LocalDate.of(1895, 12, 28), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    void createFilm_withReleaseDate27Dec1895_shouldReturnError() throws Exception {
        Film film = new Film(null, "Фильм", "Описание",
                LocalDate.of(1895, 12, 27), 120L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_withDuration1_shouldReturnCreated() throws Exception {
        Film film = new Film(null, "Фильм", "Описание",
                LocalDate.of(2020, 1, 1), 1L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    void createFilm_withDuration0_shouldReturnError() throws Exception {
        Film film = new Film(null, "Фильм", "Описание",
                LocalDate.of(2020, 1, 1), 0L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_withNegativeDuration_shouldReturnError() throws Exception {
        Film film = new Film(null, "Фильм", "Описание",
                LocalDate.of(2020, 1, 1), -10L);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_withEmptyBody_shouldReturnError() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllFilms_whenEmpty_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}