package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testFindUserById() {
        User user = userStorage.findById(1);

        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("email", "user1@mail.ru")
                .hasFieldOrPropertyWithValue("login", "user1")
                .hasFieldOrPropertyWithValue("name", "User One");
    }

    @Test
    void testFindUserById_UserNotFound() {
        assertThatThrownBy(() -> userStorage.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id 999 не найден");
    }

    @Test
    void testFindAllUsers() {
        List<User> users = userStorage.findAll();

        assertThat(users)
                .isNotEmpty()
                .hasSize(3)
                .extracting(User::getEmail)
                .contains("user1@mail.ru", "user2@mail.ru", "user3@mail.ru");
    }

    @Test
    void testCreateUser() {
        User newUser = new User();
        newUser.setEmail("newuser@mail.ru");
        newUser.setLogin("newuser");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1985, 3, 15));

        User createdUser = userStorage.create(newUser);

        assertThat(createdUser)
                .isNotNull()
                .hasFieldOrProperty("id")
                .hasFieldOrPropertyWithValue("email", "newuser@mail.ru")
                .hasFieldOrPropertyWithValue("login", "newuser")
                .hasFieldOrPropertyWithValue("name", "New User")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1985, 3, 15));

        assertThat(createdUser.getId()).isNotNull().isPositive();
    }

    @Test
    void testCreateUser_WithEmptyName() {
        User newUser = new User();
        newUser.setEmail("emptyname@mail.ru");
        newUser.setLogin("emptylogin");
        newUser.setName("");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(newUser);

        assertThat(createdUser)
                .hasFieldOrPropertyWithValue("name", "emptylogin");
    }

    @Test
    void testCreateUser_WithNullName() {
        User newUser = new User();
        newUser.setEmail("nullname@mail.ru");
        newUser.setLogin("nulllogin");
        newUser.setName(null);
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(newUser);

        assertThat(createdUser)
                .hasFieldOrPropertyWithValue("name", "nulllogin");
    }

    @Test
    void testUpdateUser() {
        User userToUpdate = userStorage.findById(1);
        userToUpdate.setName("Updated Name");
        userToUpdate.setEmail("updated@mail.ru");

        User updatedUser = userStorage.update(userToUpdate);

        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@mail.ru");

        // Проверяем, что изменения сохранены в базе
        User userFromDb = userStorage.findById(1);
        assertThat(userFromDb)
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@mail.ru");
    }

    @Test
    void testUpdateUser_UserNotFound() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999);
        nonExistentUser.setEmail("test@mail.ru");
        nonExistentUser.setLogin("test");
        nonExistentUser.setName("Test");
        nonExistentUser.setBirthday(LocalDate.of(1990, 1, 1));

        assertThatThrownBy(() -> userStorage.update(nonExistentUser))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id 999 не найден");
    }

    @Test
    void testDeleteUser() {
        User newUser = new User();
        newUser.setEmail("todelete@mail.ru");
        newUser.setLogin("todelete");
        newUser.setName("To Delete");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(newUser);
        int userId = createdUser.getId();

        userStorage.delete(userId);

        assertThatThrownBy(() -> userStorage.findById(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        assertThatThrownBy(() -> userStorage.delete(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id 999 не найден");
    }

    @Test
    void testAddFriend_NewFriendship() {
        // Создаём двух новых пользователей
        User user1 = new User();
        user1.setEmail("friend1@mail.ru");
        user1.setLogin("friend1");
        user1.setName("Friend One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("friend2@mail.ru");
        user2.setLogin("friend2");
        user2.setName("Friend Two");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        user2 = userStorage.create(user2);

        userStorage.addFriend(user1.getId(), user2.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        assertThat(updatedUser1.getFriends())
                .containsKey(user2.getId())
                .containsEntry(user2.getId(), FriendshipStatus.UNCONFIRMED);
    }

    @Test
    void testAddFriend_ConfirmedFriendship() {
        // Создаём двух новых пользователей
        User user1 = new User();
        user1.setEmail("mutual1@mail.ru");
        user1.setLogin("mutual1");
        user1.setName("Mutual One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userStorage.create(user1);
        int user1Id = user1.getId();

        User user2 = new User();
        user2.setEmail("mutual2@mail.ru");
        user2.setLogin("mutual2");
        user2.setName("Mutual Two");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        user2 = userStorage.create(user2);
        int user2Id = user2.getId();

        // Первый пользователь отправляет заявку
        userStorage.addFriend(user1Id, user2Id);

        // Второй пользователь принимает заявку
        userStorage.addFriend(user2Id, user1Id);

        // Проверяем, что дружба подтверждена с обеих сторон
        User updatedUser1 = userStorage.findById(user1Id);
        User updatedUser2 = userStorage.findById(user2Id);

        assertThat(updatedUser1.getFriends())
                .containsEntry(user2Id, FriendshipStatus.CONFIRMED);

        assertThat(updatedUser2.getFriends())
                .containsEntry(user1Id, FriendshipStatus.CONFIRMED);
    }

    @Test
    void testRemoveFriend() {
        // Используем существующую дружбу между пользователями 1 и 2
        // removeFriend удаляет только одностороннюю связь
        userStorage.removeFriend(1, 2);

        User user1 = userStorage.findById(1);
        User user2 = userStorage.findById(2);

        // Пользователь 1 удалил пользователя 2 из друзей (односторонне)
        assertThat(user1.getFriends()).doesNotContainKey(2);

        // Пользователь 2 всё ещё имеет пользователя 1 в друзьях
        assertThat(user2.getFriends()).containsKey(1);
    }

    @Test
    void testAddFriend_UserNotFound() {
        assertThatThrownBy(() -> userStorage.addFriend(999, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testAddFriend_FriendNotFound() {
        assertThatThrownBy(() -> userStorage.addFriend(1, 999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testRemoveFriend_UserNotFound() {
        assertThatThrownBy(() -> userStorage.removeFriend(999, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testLoadFriendsCorrectly() {
        User user1 = userStorage.findById(1);

        assertThat(user1.getFriends())
                .hasSize(2)
                .containsEntry(2, FriendshipStatus.CONFIRMED)
                .containsEntry(3, FriendshipStatus.UNCONFIRMED);
    }
}
