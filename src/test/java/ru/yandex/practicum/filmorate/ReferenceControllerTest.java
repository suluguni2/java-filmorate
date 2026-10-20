package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReferenceControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void referenceEndpointsReturnOrderedDictionaries() throws Exception {
        mvc.perform(get("/genres")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/genres/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Комедия"));
        mvc.perform(get("/mpa")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5));
        mvc.perform(get("/mpa/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("G"));
        mvc.perform(get("/genres/99")).andExpect(status().isNotFound());
        mvc.perform(get("/mpa/99")).andExpect(status().isNotFound());
    }

    @Test
    void invalidReferencesAreRejectedBeforeSavingFilm() throws Exception {
        mvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Film\",\"mpa\":{\"id\":99}}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Film\",\"mpa\":{\"id\":1},\"genres\":[{\"id\":99}]}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Film\",\"mpa\":{}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/films")).andExpect(content().json("[]"));
    }

    @Test
    void malformedJsonAndInvalidPathReturnBadRequest() throws Exception {
        mvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/mpa/invalid")).andExpect(status().isBadRequest());
    }
}
