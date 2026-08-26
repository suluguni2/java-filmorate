package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
@Import({FilmService.class, UserService.class, InMemoryFilmStorage.class, InMemoryUserStorage.class})
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmStorage filmStorage;

    private Film createFilm(Film film) throws Exception {
        String body = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, Film.class);
    }

    private User createUserInStorage(String login) {
        return userStorage.create(new User(null, login + "@mail.ru", login, login, LocalDate.of(1990, 1, 1)));
    }

    @BeforeEach
    void setUp() {
        filmController.clearFilms();
    }

    @Test
    void addLike_success() throws Exception {
        Film film = createFilm(new Film(null, "Фильм", "Описание", LocalDate.of(2020, 1, 1), 120L));
        User user = createUserInStorage("liker");

        mockMvc.perform(put("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

        assertEquals(1, filmStorage.getLikesCount(film.getId()));
    }

    @Test
    void addLike_filmNotFound_returns404() throws Exception {
        User user = createUserInStorage("liker");
        mockMvc.perform(put("/films/999/like/" + user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLike_userNotFound_returns404() throws Exception {
        Film film = createFilm(new Film(null, "Фильм", "Описание", LocalDate.of(2020, 1, 1), 120L));
        mockMvc.perform(put("/films/" + film.getId() + "/like/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeLike_success() throws Exception {
        Film film = createFilm(new Film(null, "Фильм", "Описание", LocalDate.of(2020, 1, 1), 120L));
        User user = createUserInStorage("liker");

        mockMvc.perform(put("/films/" + film.getId() + "/like/" + user.getId())).andExpect(status().isOk());
        mockMvc.perform(delete("/films/" + film.getId() + "/like/" + user.getId())).andExpect(status().isOk());

        assertEquals(0, filmStorage.getLikesCount(film.getId()));
    }

    @Test
    void getPopularFilms_sortedByLikesDescending() throws Exception {
        Film first = createFilm(new Film(null, "Первый", "Описание", LocalDate.of(2020, 1, 1), 120L));
        Film second = createFilm(new Film(null, "Второй", "Описание", LocalDate.of(2020, 1, 1), 120L));
        User u1 = createUserInStorage("u1");
        User u2 = createUserInStorage("u2");

        mockMvc.perform(put("/films/" + first.getId() + "/like/" + u1.getId())).andExpect(status().isOk());
        mockMvc.perform(put("/films/" + first.getId() + "/like/" + u2.getId())).andExpect(status().isOk());
        mockMvc.perform(put("/films/" + second.getId() + "/like/" + u1.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.getId()))
                .andExpect(jsonPath("$[1].id").value(second.getId()));
    }

    @Test
    void getPopularFilms_negativeCount_returns400() throws Exception {
        mockMvc.perform(get("/films/popular").param("count", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFilm_success_andNotFoundAfter() throws Exception {
        Film film = createFilm(new Film(null, "Фильм", "Описание", LocalDate.of(2020, 1, 1), 120L));

        mockMvc.perform(delete("/films/" + film.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/films/" + film.getId())).andExpect(status().isNotFound());
    }

    @Test
    void clearFilms_shouldResetIdsAndLikes() throws Exception {
        Film film = createFilm(new Film(null, "Фильм", "Описание", LocalDate.of(2020, 1, 1), 120L));
        User user = createUserInStorage("liker");
        mockMvc.perform(put("/films/" + film.getId() + "/like/" + user.getId())).andExpect(status().isOk());

        filmController.clearFilms();

        Film newFilm = createFilm(new Film(null, "Новый", "Описание", LocalDate.of(2020, 1, 1), 120L));
        assertEquals(1, newFilm.getId());                              // id начался заново
        assertEquals(0, filmStorage.getLikesCount(newFilm.getId()));   // старые лайки не «воскресли»
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