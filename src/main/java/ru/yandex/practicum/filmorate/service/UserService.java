package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private static final String EMAIL_PATTERN = "@";

    private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Пользователь с id={} пытается добавить в друзья самого себя", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        return userStorage.getCommonFriends(userId, otherUserId);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(long userId) {
        return userStorage.findById(userId);
    }

    public User create(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.error("Id пользователя не указан при обновлении");
            throw new ValidationException("Id пользователя должен быть указан");
        }
        validateUser(user);
        return userStorage.update(user);
    }

    public void clearUsers() {
        userStorage.clearUsers();
    }

    public void deleteUser(long userId) {
        userStorage.deleteUser(userId);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Электронная почта не может быть пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains(EMAIL_PATTERN)) {
            log.error("Электронная почта должна содержать символ {}", EMAIL_PATTERN);
            throw new ValidationException("Электронная почта должна содержать символ " + EMAIL_PATTERN);
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Логин не может быть пустым");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин не должен содержать пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
