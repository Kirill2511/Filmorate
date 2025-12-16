package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("GET /directors - получение всех режиссеров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        log.info("GET /directors/{} - получение режиссера по id", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director addDirector(@Validated(OnCreate.class) @RequestBody Director director) {
        log.info("POST /directors - добавление режиссера");
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Validated(OnUpdate.class) @RequestBody Director director) {
        log.info("PUT /directors - обновление режиссера {}", director.getId());
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Integer id) {
        log.info("DELETE /directors/{} - удаление режиссера", id);
        directorService.deleteDirector(id);
    }
}
