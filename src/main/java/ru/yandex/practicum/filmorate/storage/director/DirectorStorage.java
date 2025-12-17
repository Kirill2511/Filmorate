package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

    List<Director> getAllDirectors();

    Optional<Director> getDirectorById(Integer id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(Integer id);

    boolean isDirectorPresent(Integer id);
}
