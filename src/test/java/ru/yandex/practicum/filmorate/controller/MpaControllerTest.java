package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MpaController.class)
public class MpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("removal")
    private MpaService mpaService;

    @Test
    void getAllMpa_shouldReturnListOfMpaRatings() throws Exception {
        List<Mpa> mpaList = Arrays.asList(
                new Mpa(1, "G"),
                new Mpa(2, "PG"),
                new Mpa(3, "PG-13"),
                new Mpa(4, "R"),
                new Mpa(5, "NC-17")
        );

        when(mpaService.getAllMpa()).thenReturn(mpaList);

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[4].id").value(5))
                .andExpect(jsonPath("$[4].name").value("NC-17"));
    }

    @Test
    void getMpaById_shouldReturnMpa_whenMpaExists() throws Exception {
        Mpa mpa = new Mpa(1, "G");

        when(mpaService.getMpaById(1)).thenReturn(mpa);

        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));
    }

    @Test
    void getMpaById_shouldReturn404_whenMpaDoesNotExist() throws Exception {
        when(mpaService.getMpaById(999)).thenThrow(new NotFoundException("Рейтинг MPA с id 999 не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound());
    }
}
