package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private static final String EMAIL_PATTERN = "@";

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;
    private final Map<Long, Set<Long>> userFriends = new HashMap<>();

    public void clearUsers() {
        users.clear();
        nextId = 1;
    }

    public boolean isUserExist(long userId) {
        return users.containsKey(userId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!users.containsKey(friendId)) {
            log.error("Пользователь с id={} не найден", friendId);
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        userFriends.computeIfAbsent(userId, id -> new HashSet<>()).add(friendId);
        userFriends.computeIfAbsent(friendId, id -> new HashSet<>()).add(userId);
    }

    @Override
    public List<User> getFriends(long userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Set<Long> friendsIds = userFriends.get(userId);
        if (friendsIds == null) {
            return Collections.emptyList();
        }

        return friendsIds.stream()
                .map(users::get)
                .toList();
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!users.containsKey(friendId)) {
            log.error("Пользователь с id={} не найден", friendId);
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        if (userFriends.get(userId) != null) {
            userFriends.get(userId).remove(friendId);
        }
        if (userFriends.get(friendId) != null) {
            userFriends.get(friendId).remove(userId);
        }
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!users.containsKey(otherUserId)) {
            log.error("Пользователь с id={} не найден", otherUserId);
            throw new NotFoundException("Пользователь с id=" + otherUserId + " не найден");
        }
        Set<Long> friends1 = userFriends.get(userId);
        Set<Long> friends2 = userFriends.get(otherUserId);

        if (friends1 == null || friends2 == null) {
            return Collections.emptyList();
        }
        return friends1.stream()
                .filter(friends2::contains)
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return users.values();
    }

    @Override
    public User create(User user) {
        log.info("Получен запрос на создание пользователя с логином: {}", user.getLogin());

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не указано, установлено значение логина: {}", user.getLogin());
        }

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Пользователь успешно создан с id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Получен запрос на обновление пользователя с id={}", user.getId());

        if (user.getId() == null) {
            log.error("Id пользователя не указан при обновлении");
            throw new ValidationException("Id пользователя должен быть указан");
        }

        validateUser(user);

        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id={} успешно обновлён", user.getId());
        return user;
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
