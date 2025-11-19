package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        log.debug("Создан пользователь с id: {}", user.getId());
        return findById(user.getId());
    }

    @Override
    public User update(User user) {
        findById(user.getId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        log.debug("Обновлён пользователь с id: {}", user.getId());
        return findById(user.getId());
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT user_id, email, login, name, birthday FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper());

        // Загружаем друзей для каждого пользователя
        for (User user : users) {
            Map<Integer, FriendshipStatus> friends = loadFriends(user.getId());
            for (Map.Entry<Integer, FriendshipStatus> entry : friends.entrySet()) {
                user.addFriend(entry.getKey(), entry.getValue());
            }
        }

        log.debug("Получен список всех пользователей, количество: {}", users.size());
        return users;
    }

    @Override
    public User findById(Integer id) {
        String sql = "SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), id);

        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        User user = users.getFirst();
        Map<Integer, FriendshipStatus> friends = loadFriends(id);
        for (Map.Entry<Integer, FriendshipStatus> entry : friends.entrySet()) {
            user.addFriend(entry.getKey(), entry.getValue());
        }
        log.debug("Получен пользователь с id: {}", id);
        return user;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        log.debug("Удалён пользователь с id: {}", id);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        findById(userId);
        findById(friendId);

        // Проверяем, есть ли уже заявка от друга
        String checkSql = "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?";
        List<String> existingStatuses = jdbcTemplate.query(checkSql,
                (rs, rowNum) -> rs.getString("status"),
                friendId, userId);

        if (!existingStatuses.isEmpty() && "UNCONFIRMED".equals(existingStatuses.getFirst())) {
            // Друг уже отправил заявку - подтверждаем обе стороны
            String updateSql = "UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(updateSql, FriendshipStatus.CONFIRMED.name(), friendId, userId);

            // Вставляем или обновляем обратную связь
            String insertOrUpdateSql = "MERGE INTO friendship (user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertOrUpdateSql, userId, friendId, FriendshipStatus.CONFIRMED.name());
            log.debug("Подтверждена дружба между пользователями {} и {}", userId, friendId);
        } else {
            // Добавляем новую заявку со статусом UNCONFIRMED
            String insertSql = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, friendId, FriendshipStatus.UNCONFIRMED.name());
            log.debug("Пользователь {} отправил заявку в друзья пользователю {}", userId, friendId);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        findById(userId);
        findById(friendId);

        // Удаляем только одностороннюю связь от userId к friendId
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);

        log.debug("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        };
    }

    private Map<Integer, FriendshipStatus> loadFriends(int userId) {
        String sql = "SELECT friend_id, status FROM friendship WHERE user_id = ?";
        Map<Integer, FriendshipStatus> friends = new HashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            int friendId = rs.getInt("friend_id");
            FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
            friends.put(friendId, status);
        }, userId);

        return friends;
    }
}
