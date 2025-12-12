package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.params.SortBy;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String BASE_SELECT_QUERY = """
            SELECT
                f.film_id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name AS mpa_name,
                ARRAY_AGG(DISTINCT l.user_id) AS likes,
                ARRAY_AGG(DISTINCT g.genre_id ORDER BY g.genre_id) AS genre_ids,
                ARRAY_AGG(DISTINCT g.name ORDER BY g.genre_id) AS genre_names,
                ARRAY_AGG(DISTINCT d.director_id ORDER BY d.director_id) AS director_ids,
                ARRAY_AGG(DISTINCT d.name ORDER BY d.director_id) AS director_names,
                COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating m ON f.mpa_id = m.mpa_id
            LEFT JOIN film_likes AS l ON f.film_id = l.film_id
            LEFT JOIN film_genre AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id
            LEFT JOIN directors AS d ON d.director_id = fd.director_id
            """;

    private static final String GROUP_BY = """
            GROUP BY
                f.film_id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name
            """;

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            addFilmDirectors(film.getId(), film.getDirectors());
        }

        log.debug("Создан фильм с id: {}", film.getId());
        return findById(film.getId());
    }

    @Override
    public Film update(Film film) {
        findById(film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        // Обновляем жанры
        String deleteGenresSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        // Обновляем режиссеров
        String deleteDirectorsSql = "DELETE from film_directors WHERE film_id = ?";
        jdbcTemplate.update(deleteDirectorsSql, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            addFilmDirectors(film.getId(), film.getDirectors());
        }

        log.debug("Обновлён фильм с id: {}", film.getId());
        return findById(film.getId());
    }

    @Override
    public List<Film> findAll() {
        String sql = BASE_SELECT_QUERY + "\n" + GROUP_BY;

        List<Film> films = jdbcTemplate.query(sql, mapper);

        log.debug("Получен список всех фильмов, количество: {}", films.size());
        return films;
    }

    @Override
    public Film findById(Integer id) {
        String sql = BASE_SELECT_QUERY + "\nWHERE f.film_id = ?\n" + GROUP_BY;

        List<Film> films = jdbcTemplate.query(sql, mapper, id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film film = films.getFirst();

        log.debug("Получен фильм с id: {}", id);
        return film;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        log.debug("Удалён фильм с id: {}", id);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        findById(filmId);

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);

        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        findById(filmId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);

        log.debug("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    @Override
    public List<Film> findPopularFilms(Integer limit, Integer year, Integer genreId) {
        String sql = buildQuery(year, genreId);
        Object[] params = buildParams(year, genreId, limit);

        List<Film> films = jdbcTemplate.query(sql, mapper, params);

        log.debug("Получен список популярных фильмов, количество: {}", films.size());
        return films;
    }

    /**
     * Возвращает список фильмов указанного режиссера с сортировкой.
     * <p>
     * Поведение:
     * 1. В зависимости от параметра sortBy формирует фрагмент ORDER BY:
     * likes — сортировка по количеству лайков по убыванию;
     * year  — сортировка по дате выхода фильма по возрастанию.
     * 2. Собирает итоговый SQL-запрос на основе базового SELECT, условия по director_id
     * и группировки (GROUP BY), а затем добавляет секцию сортировки.
     * 3. Выполняет запрос через JdbcTemplate и маппер, возвращая список фильмов.
     *
     * @param directorId     идентификатор режиссера
     * @param sortBy тип сортировки (likes или year)
     * @return список фильмов, отсортированных по заданному правилу
     */
    @Override
    public List<Film> getFilmsByDirector(Integer directorId, SortBy sortBy) {
        String sqlSortBy = "";
        switch (sortBy) {
            case likes -> sqlSortBy = "ORDER BY likes_count DESC";
            case year -> sqlSortBy = "ORDER BY f.release_date";
            default -> throw new IllegalArgumentException("Такая сортировка не поддерживается: " + sortBy);
        }
        String sql = BASE_SELECT_QUERY + "\nWHERE d.director_id = ?\n" + GROUP_BY + "\n" + sqlSortBy + "\n";

        List<Film> films = jdbcTemplate.query(sql, mapper, directorId);
        log.debug("Получен список фильмов режиссера, количество: {}", films.size());
        return films;
    }

    /**
     * Добавляет связь "фильм — режиссер" для переданного списка режиссеров.
     * <p>
     * Метод выполняет пакетную вставку (batch update), что значительно быстрее,
     * чем отправлять INSERT по одному.
     * <p>
     * Поведение:
     * 1. Вызывает batchUpdate, передавая список режиссеров:
     * — для каждого элемента списка будет выполнена одна "порция" INSERT'а;
     * — PreparedStatementSetter устанавливает параметры film_id и director_id.
     * 2. В результате в таблицу film_directors добавляется по одной записи
     * на каждого режиссера из списка.
     *
     * @param filmId    идентификатор фильма
     * @param directors список режиссеров, которых необходимо привязать к фильму
     */
    private void addFilmDirectors(int filmId, List<Director> directors) {
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, directors, directors.size(),
                (ps, director) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, director.getId());
                });
    }

    /**
     * Строит запрос, основываясь на наличии года и жанра
     */
    private String buildQuery(Integer year, Integer genreId) {
        StringBuilder baseQuery = new StringBuilder(BASE_SELECT_QUERY);

        List<String> conditions = new ArrayList<>();

        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
        }
        if (genreId != null) {
            conditions.add("fg.genre_id = ?");
        }

        if (!conditions.isEmpty()) {
            baseQuery.append("WHERE ")
                    .append(String.join(" AND ", conditions))
                    .append("\n");
        }
        baseQuery.append(GROUP_BY).append("\nORDER BY likes_count DESC LIMIT ?");

        return baseQuery.toString();
    }

    private Object[] buildParams(Integer year, Integer genreId, Integer limit) {
        List<Integer> params = new ArrayList<>();
        if (year != null) {
            params.add(year);
        }
        if (genreId != null) {
            params.add(genreId);
        }
        params.add(limit);

        return params.toArray();
    }

    private void saveGenres(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : genres) {
            batchArgs.add(new Object[]{filmId, genre.getId()});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
