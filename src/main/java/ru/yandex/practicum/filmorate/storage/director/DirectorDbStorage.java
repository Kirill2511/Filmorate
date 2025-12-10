package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAllDirectors() {
        String sql = """
                SELECT
                director_id,
                name
                FROM directors
                """;

        return jdbcTemplate.query(sql, getDirectorRowMapper());
    }

    @Override
    public Optional<Director> getDirectorById(Integer id) {
        String sql = """
                SELECT
                director_id,
                name
                FROM directors
                WHERE director_id = ?
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getDirectorRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            director.setId(id);
            return director;
        } else {
            throw new InternalServerException("Ошибка при добавлении режиссера");
        }
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";

        int rowsUpdated = jdbcTemplate.update(sql, director.getName(), director.getId());
        if (rowsUpdated == 0) {
            throw new InternalServerException("Ошибка при обновлении режиссера");
        }
        return getDirectorById(director.getId()).orElseThrow(()
                -> new InternalServerException("Режиссер не найден после обновления"));
    }

    @Override
    public void deleteDirector(Integer id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean isDirectorPresent(Integer id) {
        return getDirectorById(id).isPresent();
    }

    private RowMapper<Director> getDirectorRowMapper() {
        return (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setName(rs.getString("name"));

            return director;
        };
    }

}
