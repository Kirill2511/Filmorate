package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.params.SortBy;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10")
                                      @Positive(message = "Count should be positive integer") Integer count,
                                      @RequestParam(required = false)
                                      @Min(value = 1895, message = "Year should be after or equal to 1895") Integer year,
                                      @RequestParam(required = false)
                                      @Positive(message = "genreId should be positive integer") Integer genreId) {
        return filmService.getPopularFilms(count, year, genreId);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId,friendId);
    }

    /**
     * Возвращает список фильмов конкретного режиссера с возможностью сортировки.
     * GET /films/director/{directorId}?sortBy=likes|year
     * Параметры:
     *
     * @param directorId — идентификатор режиссера, фильмы которого нужно получить.
     * @param sortBy     — критерий сортировки (по умолчанию "likes"):
     *                   likes — сортировать по количеству лайков (по убыванию)
     *                   year  — сортировать по году выпуска (по возрастанию)
     * @return список фильмов режиссера, отсортированный по заданному критерию.
     */
    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Integer directorId,
                                         @RequestParam(defaultValue = "likes") SortBy sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}
