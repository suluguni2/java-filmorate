package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    final UserService userService;

    @DeleteMapping
    public void clearUsers() {
        userService.clearUsers();
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        return userService.update(user);
    }

    @PostMapping("{userId}/friend/{friendId}")
    public void addFriend(@PathVariable long userId,
                          @PathVariable long friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("{userId}/friend/{friendId}")
    public void removeFriend(@PathVariable long userId,
                             @PathVariable long friendId) {
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("{userId}/friends")
    public List<User> getFriends(@PathVariable long userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("{userId}/common-friends/{otherUserId}")
    public List<User> getCommonFriends(@PathVariable long userId,
                                       @PathVariable long otherUserId) {
        return userService.getCommonFriends(userId,  otherUserId);
    }
}