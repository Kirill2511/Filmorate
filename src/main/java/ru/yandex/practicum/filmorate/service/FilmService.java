package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

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
        userService.getUserById(userId); // Проверяем существование пользователя
        filmStorage.addLike(filmId, userId);

        Film film = filmStorage.findById(filmId);
        log.info("Пользователь {} поставил лайк фильму {} (всего лайков: {})",
                userId, filmId, film.getLikes().size());
    }

    /**
     * Удалить лайк
     */
    public void removeLike(Integer filmId, Integer userId) {
        userService.getUserById(userId); // Проверяем существование пользователя
        filmStorage.removeLike(filmId, userId);

        Film film = filmStorage.findById(filmId);
        log.info("Пользователь {} удалил лайк у фильма {} (осталось лайков: {})",
                userId, filmId, film.getLikes().size());
    }

    /**
     * Получить список из первых count фильмов по количеству лайков
     */
    public List<Film> getPopularFilms(Integer count) {
        int limit = (count != null && count > 0) ? count : 10;
        return filmStorage.findPopularFilms(limit);
    }

    public List<Film> getCommonFilms(int userId,int friendId) {
        log.info("Запрос на общие фильмы");
        return filmStorage.getCommonFilms(userId,friendId);

    }
}
