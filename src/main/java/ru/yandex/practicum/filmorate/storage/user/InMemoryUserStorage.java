package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;
    private final Map<Long, Map<Long, FriendshipStatus>> userFriends = new HashMap<>();

    @Override
    public void clearUsers() {
        users.clear();
        userFriends.clear();
        nextId = 1;
    }

    @Override
    public void deleteUser(long userId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        users.remove(userId);
        userFriends.remove(userId);
        userFriends.values().forEach(friends -> friends.remove(userId));
    }

    @Override
    public boolean isUserExist(long userId) {
        return users.containsKey(userId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!isUserExist(friendId)) {
            log.error("Пользователь с id={} не найден", friendId);
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
        userFriends.computeIfAbsent(userId, id -> new HashMap<>()).put(friendId, FriendshipStatus.CONFIRMED);
        userFriends.computeIfAbsent(friendId, id -> new HashMap<>()).put(userId, FriendshipStatus.CONFIRMED);
    }

    @Override
    public List<User> getFriends(long userId) {
        log.info("Запрошен список друзей пользователя с id={}", userId);
        if (!isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Map<Long, FriendshipStatus> friendsIds = userFriends.get(userId);
        if (friendsIds == null) {
            return Collections.emptyList();
        }
        return friendsIds
                .keySet()
                .stream()
                .map(users::get)
                .toList();
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!isUserExist(friendId)) {
            log.error("Пользователь с id={} не найден", friendId);
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        Map<Long, FriendshipStatus> userFriendMap = userFriends.get(userId);
        if (userFriendMap != null) {
            userFriendMap.remove(friendId);
        }

        userFriendMap = userFriends.get(friendId);
        if (userFriendMap != null) {
            userFriendMap.remove(userId);
        }
        log.info("Пользователь с id={} удаляет из друзей пользователя с id={}", userId, friendId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        log.info("Запрошен список общих друзей для пользователей {} и {}", userId, otherUserId);
        if (!isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!isUserExist(otherUserId)) {
            log.error("Пользователь с id={} не найден", otherUserId);
            throw new NotFoundException("Пользователь с id=" + otherUserId + " не найден");
        }
        Map<Long, FriendshipStatus> friends1 = userFriends.get(userId);
        Map<Long, FriendshipStatus> friends2 = userFriends.get(otherUserId);

        if (friends1 == null || friends2 == null) {
            return Collections.emptyList();
        }
        return friends1.keySet()
                .stream()
                .filter(friends2::containsKey)
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return users.values();
    }

    @Override
    public User findById(long userId) {
        if (!isUserExist(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return users.get(userId);
    }

    @Override
    public User create(User user) {
        log.info("Получен запрос на создание пользователя с логином: {}", user.getLogin());

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
}
