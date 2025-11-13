package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(MpaDbStorage.class)
@ActiveProfiles("test")
public class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void findAll_shouldReturnAllMpaRatings() {
        List<Mpa> mpaList = mpaDbStorage.findAll();

        assertThat(mpaList).isNotEmpty();
        assertThat(mpaList).hasSize(5);
        assertThat(mpaList).extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void findById_shouldReturnMpa_whenMpaExists() {
        Mpa mpa = mpaDbStorage.findById(1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void findById_shouldThrowNotFoundException_whenMpaDoesNotExist() {
        assertThatThrownBy(() -> mpaDbStorage.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Рейтинг MPA с id 999 не найден");
    }

    @Test
    void findAll_shouldReturnMpaOrderedById() {
        List<Mpa> mpaList = mpaDbStorage.findAll();

        assertThat(mpaList).extracting(Mpa::getId)
                .containsExactly(1, 2, 3, 4, 5);
    }
}
