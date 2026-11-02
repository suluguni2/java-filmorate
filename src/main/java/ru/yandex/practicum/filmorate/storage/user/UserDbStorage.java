package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;

@Repository("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL = "SELECT * FROM users ORDER BY id";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public User findById(long userId) {
        return findOne(FIND_BY_ID, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    @Override
    public User create(User user) {
        user.setId(insert(INSERT, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday()));
        return user;
    }

    @Override
    public User update(User user) {
        update(UPDATE, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public void deleteUser(long userId) {
        update("DELETE FROM users WHERE id = ?", userId);
    }

    @Override
    public boolean isUserExist(long userId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId) > 0;
    }

    @Override
    public void clearUsers() {
        jdbc.update("DELETE FROM users");
    }

    @Override
    @Transactional
    public void addFriend(long userId, long friendId) {
        findById(userId);
        findById(friendId);
        boolean reciprocal = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users_friends WHERE user_id = ? AND friend_id = ?",
                Integer.class, friendId, userId) > 0;
        String status = reciprocal ? "CONFIRMED" : "UNCONFIRMED";
        jdbc.update("MERGE INTO users_friends(user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, ?)",
                userId, friendId, status);
        if (reciprocal) {
            jdbc.update("UPDATE users_friends SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
        }
    }

    @Override
    @Transactional
    public void removeFriend(long userId, long friendId) {
        findById(userId);
        findById(friendId);
        jdbc.update("DELETE FROM users_friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
        jdbc.update("UPDATE users_friends SET status = 'UNCONFIRMED' WHERE user_id = ? AND friend_id = ?",
                friendId, userId);
    }

    @Override
    public List<User> getFriends(long userId) {
        findById(userId);
        return findMany("SELECT u.* FROM users u JOIN users_friends f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? ORDER BY u.id", userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        findById(userId);
        findById(otherUserId);
        return findMany("SELECT u.* FROM users u JOIN users_friends a ON u.id = a.friend_id " +
                "JOIN users_friends b ON u.id = b.friend_id WHERE a.user_id = ? AND b.user_id = ? ORDER BY u.id",
                userId, otherUserId);
    }
}
