package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    /**
     * Создать фильм
     */
    public Film createFilm(Film film) {
        Film createdFilm = filmStorage.create(film);
        log.info("Создан фильм: id={}, name={}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    /**
     * Обновить фильм
     */
    public Film updateFilm(Film film) {
        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлён фильм: id={}, name={}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    /**
     * Получить все фильмы
     */
    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    /**
     * Получить фильм по ID
     */
    public Film getFilmById(Integer id) {
        return filmStorage.findById(id);
    }

    /**
     * Поставить лайк фильму
     */
    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId); // Проверяем существование пользователя

        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {} (всего лайков: {})",
                userId, filmId, film.getLikes().size());
    }

    /**
     * Удалить лайк
     */
    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId); // Проверяем существование пользователя

        film.getLikes().remove(userId);
        log.info("Пользователь {} удалил лайк у фильма {} (осталось лайков: {})",
                userId, filmId, film.getLikes().size());
    }

    /**
     * Получить список из первых count фильмов по количеству лайков
     */
    public List<Film> getPopularFilms(Integer count) {
        int limit = (count != null && count > 0) ? count : 10;

        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
