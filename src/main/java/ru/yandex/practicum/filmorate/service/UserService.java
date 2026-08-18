package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    final private UserStorage inMemoryUserStorage;

    public void addFriend(long userId, long friendId) {
        inMemoryUserStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        inMemoryUserStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        return inMemoryUserStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        return inMemoryUserStorage.getCommonFriends(userId,  otherUserId);
    }
}
