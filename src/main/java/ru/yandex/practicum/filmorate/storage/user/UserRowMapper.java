package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.setId(rs.getLong("ID"));
        user.setEmail(rs.getString("EMAIL"));
        user.setName(rs.getString("NAME"));
        user.setLogin(rs.getString("LOGIN"));
        user.setBirthday(rs.getObject("BIRTHDAY", LocalDate.class));

        return user;
    }
}