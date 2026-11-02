package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.*;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class, MpaDbStorage.class, MpaRowMapper.class})
class StorageTest {
    @Autowired
    private UserDbStorage users;
    @Autowired
    private FilmDbStorage films;
    @Autowired
    private GenreDbStorage genres;
    @Autowired
    private MpaDbStorage mpa;
    @Autowired
    private JdbcTemplate jdbc;

    private User user(String login) {
        return users.create(new User(null, login + "@mail.ru", login, login, null));
    }

    private Film film(String name) {
        return films.create(new Film(null, name, "Описание", LocalDate.of(2020, 1, 1), 90L,
                new LinkedHashSet<>(List.of(new Genre(2L, null), new Genre(1L, null))), new Mpa(1L, null)));
    }

    @Test
    void userCrudAndNullableBirthday() {
        User user = user("first");
        assertThat(users.isUserExist(user.getId())).isTrue();
        assertThat(users.findById(user.getId())).isEqualTo(user);
        assertThat(users.findAll()).containsExactly(user);
        user.setName("Новое имя");
        user.setBirthday(LocalDate.of(1995, 5, 5));
        users.update(user);
        assertThat(users.findById(user.getId())).isEqualTo(user);
        users.deleteUser(user.getId());
        assertThat(users.isUserExist(user.getId())).isFalse();
        assertThatThrownBy(() -> users.findById(user.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> users.update(user)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> users.deleteUser(user.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void friendshipIsDirectedAndRepeatedRequestsAreSafe() {
        User first = user("first");
        User second = user("second");
        User common = user("common");
        users.addFriend(first.getId(), second.getId());
        users.addFriend(first.getId(), second.getId());
        assertThat(users.getFriends(first.getId())).containsExactly(second);
        assertThat(users.getFriends(second.getId())).isEmpty();
        users.addFriend(second.getId(), first.getId());
        assertThat(jdbc.queryForList("SELECT status FROM users_friends", String.class))
                .containsOnly("CONFIRMED");
        users.removeFriend(first.getId(), second.getId());
        users.removeFriend(first.getId(), second.getId());
        assertThat(users.getFriends(first.getId())).isEmpty();
        assertThat(users.getFriends(second.getId())).containsExactly(first);
        assertThat(jdbc.queryForObject("SELECT status FROM users_friends", String.class)).isEqualTo("UNCONFIRMED");
        users.addFriend(first.getId(), common.getId());
        users.addFriend(second.getId(), common.getId());
        assertThat(users.getCommonFriends(first.getId(), second.getId())).containsExactly(common);
        users.deleteUser(common.getId());
        assertThat(users.getCommonFriends(first.getId(), second.getId())).isEmpty();
        users.clearUsers();
        assertThat(users.findAll()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users_friends", Integer.class)).isZero();
    }

    @Test
    void friendshipRejectsMissingUsers() {
        User user = user("first");
        assertThatThrownBy(() -> users.addFriend(user.getId(), -1)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> users.removeFriend(-1, user.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> users.getFriends(-1)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> users.getCommonFriends(user.getId(), -1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void filmCrudReplacesGenresAndLoadsNames() {
        Film film = film("Первый");
        assertThat(film.getMpa().getName()).isEqualTo("G");
        assertThat(film.getGenres()).extracting(Genre::getId).containsExactly(1L, 2L);
        assertThat(films.findAll()).containsExactly(film);
        assertThat(films.findById(film.getId())).isEqualTo(film);
        film.setName("Новый");
        film.setMpa(new Mpa(5L, null));
        film.setGenres(Set.of(new Genre(3L, null)));
        Film updated = films.update(film);
        assertThat(updated.getName()).isEqualTo("Новый");
        assertThat(updated.getMpa().getName()).isEqualTo("NC-17");
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(3L);
        film.setGenres(null);
        assertThat(films.update(film).getGenres()).isEmpty();
        films.deleteFilm(film.getId());
        assertThatThrownBy(() -> films.findById(film.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> films.update(film)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> films.deleteFilm(film.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void likesPopularityAndCascadingDeletion() {
        Film first = film("Первый");
        Film second = film("Второй");
        User user = user("viewer");
        films.addLike(second.getId(), user.getId());
        films.addLike(second.getId(), user.getId());
        assertThat(films.getLikesCount(second.getId())).isEqualTo(1);
        assertThat(films.getPopularFilms(1)).containsExactly(second);
        assertThat(films.getPopularFilms(0)).isEmpty();
        films.removeLike(second.getId(), user.getId());
        films.removeLike(second.getId(), user.getId());
        assertThat(films.getLikesCount(second.getId())).isZero();
        assertThat(films.getPopularFilms(10)).containsExactly(first, second);
        films.addLike(second.getId(), user.getId());
        users.deleteUser(user.getId());
        assertThat(films.getLikesCount(second.getId())).isZero();
        films.clearFilms();
        assertThat(films.findAll()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM films_genres", Integer.class)).isZero();
        assertThatThrownBy(() -> films.addLike(-1, 1)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> films.removeLike(-1, 1)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> films.getLikesCount(-1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void referenceDataIsOrderedAndMissingIdsAreRejected() {
        assertThat(genres.findAll()).extracting(Genre::getId).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(genres.findById(1).getName()).isEqualTo("Комедия");
        assertThat(mpa.findAll()).extracting(Mpa::getName).containsExactly("G", "PG", "PG-13", "R", "NC-17");
        assertThat(mpa.findById(5).getName()).isEqualTo("NC-17");
        assertThatThrownBy(() -> genres.findById(99)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> mpa.findById(99)).isInstanceOf(NotFoundException.class);
    }
}
