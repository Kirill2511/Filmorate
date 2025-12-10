package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));

        Date date = rs.getDate("release_date");
        LocalDate releaseDate = date == null ? null : date.toLocalDate();
        film.setReleaseDate(releaseDate);

        mapLikes(film, rs);
        mapMpa(film, rs);
        mapGenres(film, rs);

        return film;
    }

    private void mapLikes(Film film, ResultSet resultSet) throws SQLException {
        Array sqlArray = resultSet.getArray("likes");
        if (resultSet.wasNull()) {
            return;
        }

        Object[] idsArray = (Object[]) sqlArray.getArray();
        Set<Integer> likes = new HashSet<>();
        for (Object object : idsArray) {
            if (object != null) {
                Integer id = ((Number) object).intValue();
                likes.add(id);
            }
        }

        film.setLikes(likes);
    }

    private void mapMpa(Film film, ResultSet resultSet) throws SQLException {
        Integer mpaId = resultSet.getInt("mpa_id");
        if (!resultSet.wasNull()) {
            String mpaName = resultSet.getString("mpa_name");
            film.setMpa(new Mpa(mpaId, mpaName));
        }
    }

    private void mapGenres(Film film, ResultSet resultSet) throws SQLException {
        Array sqlArrayIds = resultSet.getArray("genre_ids");
        if (resultSet.wasNull()) {
            return;
        }

        Array sqlArrayNames = resultSet.getArray("genre_names");

        Object[] idsArray = (Object[]) sqlArrayIds.getArray();
        Object[] namesArray = (Object[]) sqlArrayNames.getArray();
        Set<Genre> genres = new LinkedHashSet<>();

        for (int i = 0; i < Math.min(idsArray.length, namesArray.length); i++) {
            if (idsArray[i] != null && namesArray[i] != null) {
                Integer id = ((Number) idsArray[i]).intValue();
                String name = namesArray[i].toString();
                genres.add(new Genre(id, name));
            }
        }
        film.setGenres(genres);
    }
}
