package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    /**
     * Создать пользователя
     */
    public User createUser(User user) {
        User createdUser = userStorage.create(user);
        log.info("Создан пользователь: id={}, login={}", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    /**
     * Обновить пользователя
     */
    public User updateUser(User user) {
        User updatedUser = userStorage.update(user);
        log.info("Обновлён пользователь: id={}, login={}", updatedUser.getId(), updatedUser.getLogin());
        return updatedUser;
    }

    /**
     * Получить всех пользователей
     */
    public List<User> getAllUsers() {
        return userStorage.findAll();
    }

    /**
     * Получить пользователя по ID
     */
    public User getUserById(Integer id) {
        return userStorage.findById(id);
    }

    /**
     * Добавить пользователя в друзья
     */
    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            log.warn("Попытка добавить себя в друзья: userId={}", userId);
            throw new IllegalArgumentException("Нельзя добавить себя в друзья");
        }

        userStorage.addFriend(userId, friendId);
        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    /**
     * Удалить пользователя из друзей
     */
    public void removeFriend(Integer userId, Integer friendId) {
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    /**
     * Получить список друзей пользователя
     */
    public List<User> getFriends(Integer userId) {
        User user = userStorage.findById(userId);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    /**
     * Получить список общих друзей
     */
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherUserId);

        Set<Integer> commonFriendsIds = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendsIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
