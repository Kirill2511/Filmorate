package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT mpa_id, name FROM mpa_rating ORDER BY mpa_id";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper());
        log.debug("Получен список всех рейтингов MPA, количество: {}", mpaList.size());
        return mpaList;
    }

    @Override
    public Mpa findById(Integer id) {
        String sql = "SELECT mpa_id, name FROM mpa_rating WHERE mpa_id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper(), id);

        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id " + id + " не найден");
        }

        log.debug("Получен рейтинг MPA с id: {}", id);
        return mpaList.getFirst();
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        };
    }
}
