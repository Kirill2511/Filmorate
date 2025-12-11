package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testFindFilmById() {
        Film film = filmStorage.findById(1);

        assertThat(film)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Test Film 1")
                .hasFieldOrPropertyWithValue("description", "Description for test film 1")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2000, 1, 1))
                .hasFieldOrPropertyWithValue("duration", 120);
        assertThat(film.getMpa().getId()).isEqualTo(1);
        assertThat(film.getMpa().getName()).isEqualTo("G");
    }

    @Test
    void testFindFilmById_FilmNotFound() {
        assertThatThrownBy(() -> filmStorage.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с id 999 не найден");
    }

    @Test
    void testFindAllFilms() {
        List<Film> films = filmStorage.findAll();

        assertThat(films)
                .isNotEmpty()
                .hasSize(3)
                .extracting(Film::getName)
                .contains("Test Film 1", "Test Film 2", "Test Film 3");
    }

    @Test
    void testCreateFilm() {
        Film newFilm = new Film();
        newFilm.setName("New Test Film");
        newFilm.setDescription("Description for new film");
        newFilm.setReleaseDate(LocalDate.of(2023, 6, 1));
        newFilm.setDuration(100);
        newFilm.setMpa(new Mpa(3, "PG-13"));

        Film createdFilm = filmStorage.create(newFilm);

        assertThat(createdFilm)
                .isNotNull()
                .hasFieldOrProperty("id")
                .hasFieldOrPropertyWithValue("name", "New Test Film")
                .hasFieldOrPropertyWithValue("description", "Description for new film")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2023, 6, 1))
                .hasFieldOrPropertyWithValue("duration", 100);
        assertThat(createdFilm.getMpa().getId()).isEqualTo(3);

        assertThat(createdFilm.getId()).isNotNull().isPositive();
    }

    @Test
    void testCreateFilm_WithGenres() {
        Film newFilm = new Film();
        newFilm.setName("Film with Genres");
        newFilm.setDescription("Test film with genres");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(120);
        newFilm.setMpa(new Mpa(4, "R"));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));
        newFilm.setGenres(genres);

        Film createdFilm = filmStorage.create(newFilm);

        assertThat(createdFilm.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactly(1, 2);
    }

    @Test
    void testUpdateFilm() {
        Film filmToUpdate = filmStorage.findById(1);
        filmToUpdate.setName("Updated Film Name");
        filmToUpdate.setDescription("Updated description");
        filmToUpdate.setDuration(150);

        Film updatedFilm = filmStorage.update(filmToUpdate);

        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Updated Film Name")
                .hasFieldOrPropertyWithValue("description", "Updated description")
                .hasFieldOrPropertyWithValue("duration", 150);

        // Проверяем, что изменения сохранены в базе
        Film filmFromDb = filmStorage.findById(1);
        assertThat(filmFromDb)
                .hasFieldOrPropertyWithValue("name", "Updated Film Name")
                .hasFieldOrPropertyWithValue("description", "Updated description");
    }

    @Test
    void testUpdateFilm_UpdateGenres() {
        Film filmToUpdate = filmStorage.findById(1);

        LinkedHashSet<Genre> newGenres = new LinkedHashSet<>();
        newGenres.add(new Genre(4, "Триллер"));
        newGenres.add(new Genre(6, "Боевик"));
        filmToUpdate.setGenres(newGenres);

        Film updatedFilm = filmStorage.update(filmToUpdate);

        assertThat(updatedFilm.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactly(4, 6);

        // Проверяем, что старые жанры удалены
        Film filmFromDb = filmStorage.findById(1);
        assertThat(filmFromDb.getGenres())
                .extracting(Genre::getId)
                .doesNotContain(1, 2);
    }

    @Test
    void testUpdateFilm_FilmNotFound() {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999);
        nonExistentFilm.setName("Non Existent");
        nonExistentFilm.setDescription("Test");
        nonExistentFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        nonExistentFilm.setDuration(100);
        nonExistentFilm.setMpa(new Mpa(1, "G"));

        assertThatThrownBy(() -> filmStorage.update(nonExistentFilm))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с id 999 не найден");
    }

    @Test
    void testDeleteFilm() {
        Film newFilm = new Film();
        newFilm.setName("Film to Delete");
        newFilm.setDescription("Will be deleted");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(100);
        newFilm.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(newFilm);
        int filmId = createdFilm.getId();

        filmStorage.delete(filmId);

        assertThatThrownBy(() -> filmStorage.findById(filmId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testDeleteFilm_FilmNotFound() {
        assertThatThrownBy(() -> filmStorage.delete(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с id 999 не найден");
    }

    @Test
    void testAddLike() {
        // Создаём новый фильм для теста
        Film newFilm = new Film();
        newFilm.setName("Film for Likes");
        newFilm.setDescription("Test likes");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(100);
        newFilm.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(newFilm);

        filmStorage.addLike(createdFilm.getId(), 3);

        Film updatedFilm = filmStorage.findById(createdFilm.getId());
        assertThat(updatedFilm.getLikes())
                .contains(3);
    }

    @Test
    void testAddLike_FilmNotFound() {
        assertThatThrownBy(() -> filmStorage.addLike(999, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testRemoveLike() {
        // Используем существующий лайк (фильм 1, пользователь 1)
        filmStorage.removeLike(1, 1);

        Film film = filmStorage.findById(1);
        assertThat(film.getLikes())
                .doesNotContain(1);
    }

    @Test
    void testRemoveLike_FilmNotFound() {
        assertThatThrownBy(() -> filmStorage.removeLike(999, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testLoadGenresCorrectly() {
        Film film1 = filmStorage.findById(1);
        assertThat(film1.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactly(1, 2);

        Film film2 = filmStorage.findById(2);
        assertThat(film2.getGenres())
                .hasSize(1)
                .extracting(Genre::getId)
                .contains(3);

        Film film3 = filmStorage.findById(3);
        assertThat(film3.getGenres())
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactly(4, 6);
    }

    @Test
    void testLoadLikesCorrectly() {
        Film film1 = filmStorage.findById(1);
        assertThat(film1.getLikes())
                .hasSize(2)
                .contains(1, 2);

        Film film2 = filmStorage.findById(2);
        assertThat(film2.getLikes())
                .hasSize(1)
                .contains(1);

        Film film3 = filmStorage.findById(3);
        assertThat(film3.getLikes())
                .isEmpty();
    }

    @Test
    void testCreateFilm_WithEmptyGenres() {
        Film newFilm = new Film();
        newFilm.setName("Film without Genres");
        newFilm.setDescription("Test film without genres");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(120);
        newFilm.setMpa(new Mpa(2, "PG"));
        newFilm.setGenres(new LinkedHashSet<>());

        Film createdFilm = filmStorage.create(newFilm);

        assertThat(createdFilm.getGenres()).isEmpty();
    }

    @Test
    void testUpdateFilm_RemoveAllGenres() {
        Film filmToUpdate = filmStorage.findById(1);
        filmToUpdate.setGenres(new LinkedHashSet<>());

        Film updatedFilm = filmStorage.update(filmToUpdate);

        assertThat(updatedFilm.getGenres()).isEmpty();
    }

    @Test
    void testGenresOrderPreserved() {
        Film newFilm = new Film();
        newFilm.setName("Film with Ordered Genres");
        newFilm.setDescription("Test genres order");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(120);
        newFilm.setMpa(new Mpa(1, "G"));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(6, "Боевик"));
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));
        newFilm.setGenres(genres);

        Film createdFilm = filmStorage.create(newFilm);

        // Жанры должны быть отсортированы по genre_id в SQL запросе
        assertThat(createdFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2, 6);
    }
}
