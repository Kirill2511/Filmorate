package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

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

        log.debug("Обновлён фильм с id: {}", film.getId());
        return findById(film.getId());
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_rating m ON f.mpa_id = m.mpa_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper());

        // Загружаем жанры и лайки для каждого фильма
        for (Film film : films) {
            film.setGenres(loadGenres(film.getId()));
            film.setLikes(loadLikes(film.getId()));
        }

        log.debug("Получен список всех фильмов, количество: {}", films.size());
        return films;
    }

    @Override
    public Film findById(Integer id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_rating m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film film = films.getFirst();
        film.setGenres(loadGenres(id));
        film.setLikes(loadLikes(id));

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
    public List<Film> findPopularFilms(int limit) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_rating m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), limit);

        for (Film film : films) {
            film.setGenres(loadGenres(film.getId()));
            film.setLikes(loadLikes(film.getId()));
        }

        log.debug("Получен список популярных фильмов, количество: {}", films.size());
        return films;
    }

    private void addFilmDirectors(int filmId, Set<Integer> directorsId) {
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, directorsId, directorsId.size(),
                (ps, directorId) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, directorId);
                });
    }

    private void saveGenres(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : genres) {
            batchArgs.add(new Object[]{filmId, genre.getId()});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        };
    }

    private LinkedHashSet<Genre> loadGenres(int filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM film_genre fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";

        List<Genre> genreList = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);

        return new LinkedHashSet<>(genreList);
    }

    private HashSet<Integer> loadLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";

        List<Integer> likesList = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId);

        return new HashSet<>(likesList);
    }
}
