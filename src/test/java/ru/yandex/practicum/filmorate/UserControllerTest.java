package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserService.class, InMemoryUserStorage.class})

class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController.clearUsers();
    }

    private User createUser(String login) throws Exception {
        User user = new User(null, login + "@mail.ru", login, login, LocalDate.of(1990, 1, 1));
        String body = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, User.class);
    }

    @Test
    void addFriend_success_andFriendAppearsInList() throws Exception {
        User user = createUser("first");
        User friend = createUser("second");

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + user.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friend.getId()));
    }

    @Test
    void addFriend_toSelf_returns400() throws Exception {
        User user = createUser("solo");
        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFriend_unknownUser_returns404() throws Exception {
        User user = createUser("first");
        mockMvc.perform(put("/users/" + user.getId() + "/friends/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFriend_success() throws Exception {
        User user = createUser("first");
        User friend = createUser("second");

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId())).andExpect(status().isOk());
        mockMvc.perform(delete("/users/" + user.getId() + "/friends/" + friend.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/users/" + user.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getCommonFriends_returnsIntersection() throws Exception {
        User user1 = createUser("first");
        User user2 = createUser("second");
        User common = createUser("common");

        mockMvc.perform(put("/users/" + user1.getId() + "/friends/" + common.getId())).andExpect(status().isOk());
        mockMvc.perform(put("/users/" + user2.getId() + "/friends/" + common.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/users/" + user1.getId() + "/friends/common/" + user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(common.getId()));
    }

    @Test
    void deleteUser_success_andNotFoundAfter() throws Exception {
        User user = createUser("todelete");

        mockMvc.perform(delete("/users/" + user.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/users/" + user.getId())).andExpect(status().isNotFound());
    }

    @Test
    void clearUsers_shouldResetIdsAndNotRestoreFriends() throws Exception {
        User user = createUser("first");
        User friend = createUser("second");
        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + friend.getId())).andExpect(status().isOk());

        userController.clearUsers();

        User newUser = createUser("third");
        assertEquals(1, newUser.getId());   // id начался заново

        mockMvc.perform(get("/users/" + newUser.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));   // старые дружеские связи не «воскресли»
    }

    @Test
    void createUser_withEmptyEmail_shouldReturnError() throws Exception {
        User user = new User(null, "", "login", "Имя",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withEmailWithoutAt_shouldReturnError() throws Exception {
        User user = new User(null, "testmail.ru", "login", "Имя",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withValidEmail_shouldReturnCreated() throws Exception {
        User user = new User(null, "test@mail.ru", "login", "Имя",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.ru"));
    }

    @Test
    void createUser_withEmptyLogin_shouldReturnError() throws Exception {
        User user = new User(null, "test@mail.ru", "", "Имя",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withLoginContainingSpaces_shouldReturnError() throws Exception {
        User user = new User(null, "test@mail.ru", "test login", "Имя",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withValidLogin_shouldReturnCreated() throws Exception {
        User user = new User(null, "test@mail.ru", "validlogin", "Имя",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_withEmptyName_shouldUseLogin() throws Exception {
        User user = new User(null, "test@mail.ru", "testlogin", "",
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void createUser_withNullName_shouldUseLogin() throws Exception {
        User user = new User(null, "test@mail.ru", "testlogin", null,
                LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void createUser_withBirthdayToday_shouldReturnCreated() throws Exception {
        User user = new User(null, "test@mail.ru", "login", "Имя", LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_withBirthdayTomorrow_shouldReturnError() throws Exception {
        User user = new User(null, "test@mail.ru", "login", "Имя",
                LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withEmptyBody_shouldReturnError() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_whenEmpty_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}