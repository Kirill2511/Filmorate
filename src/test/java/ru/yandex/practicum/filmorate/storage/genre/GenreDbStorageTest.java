package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(GenreDbStorage.class)
@ActiveProfiles("test")
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Test
    void findAll_shouldReturnAllGenres() {
        List<Genre> genres = genreDbStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSize(6);
        assertThat(genres).extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void findById_shouldReturnGenre_whenGenreExists() {
        Genre genre = genreDbStorage.findById(1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void findById_shouldThrowNotFoundException_whenGenreDoesNotExist() {
        assertThatThrownBy(() -> genreDbStorage.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Жанр с id 999 не найден");
    }

    @Test
    void findAll_shouldReturnGenresOrderedById() {
        List<Genre> genres = genreDbStorage.findAll();

        assertThat(genres).extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }
}
