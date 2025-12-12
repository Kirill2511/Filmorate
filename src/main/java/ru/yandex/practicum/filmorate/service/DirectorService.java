package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirectorById(Integer id) {
        return directorStorage.getDirectorById(id).orElseThrow(()
                -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        if (!directorStorage.isDirectorPresent(director.getId())) {
            throw new NotFoundException("Режиссер с id " + director.getId() + " не найден");
        }
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Integer id) {
        if (!directorStorage.isDirectorPresent(id)) {
            throw new NotFoundException("Режиссер с id " + id + " не найден");
        }
        directorStorage.deleteDirector(id);
    }
}
