package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final IdGenerator idGenerator = new IdGenerator();
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        validateAndSetUserName(user);
        user.setId(idGenerator.getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateAndSetUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(Integer id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public void delete(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        users.remove(id);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        // Если друг уже отправил запрос, подтверждаем дружбу
        if (friend.hasFriend(userId) &&
                friend.getFriendshipStatus(userId) == FriendshipStatus.UNCONFIRMED) {
            user.addFriend(friendId, FriendshipStatus.CONFIRMED);
            friend.addFriend(userId, FriendshipStatus.CONFIRMED);
        } else {
            // Иначе создаем неподтвержденную связь только со стороны инициатора
            user.addFriend(friendId, FriendshipStatus.UNCONFIRMED);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);
    }

    private void validateAndSetUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
